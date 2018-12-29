# 用户访问session分析-Shuffle调优

### 原理概述

**发生shuffle的情况**

- 在spark中，主要是以下几个算子：groupByKey、reduceByKey、countByKey、join，等等。

**什么是shuffle**

- `groupByKey`：要把分布在集群各个节点上的数据中的同一个key，对应的values，都给集中到一块儿，集中到集群中同一个节点上，更严密一点说，就是集中到一个节点的一个executor的一个task中。
- 然后呢，集中一个key对应的values之后，才能交给我们来进行处理，`<key, Iterable<value>>`；
- `reduceByKey`：算子函数去对values集合进行reduce操作，最后变成一个value；
- `countByKey`：需要在一个task中，获取到一个key对应的所有的value，然后进行计数，统计总共有多少个value；
- `join`：`RDD<key, value>`，`RDD<key, value>`，只要是两个RDD中，key相同对应的2个value，都能到一个节点的executor的task中，给我们进行处理。

![](img\shuffle.png)

reduceByKey(\_+\_)，问题在于，同一个单词，比如说（hello, 1），可能散落在不同的节点上；对每个单词进行累加计数，就必须让所有单词都跑到同一个节点的一个task中，给一个task来进行处理。

- 每一个shuffle的前半部分stage的task，每个task都会创建下一个stage的task数量相同的文件，比如下一个stage会有100个task，那么当前stage每个task都会创建100份文件；会将同一个key对应的values，一定是写入同一个文件中的；不同节点上的task，也一定会将同一个key对应的values，写入下一个stage，同一个task对应的文件中。
- shuffle的后半部分stage的task，每个task都会从各个节点上的task写的属于自己的那一份文件中，拉取key, value对；然后task会有一个内存缓冲区，然后会用HashMap，进行key, values的汇聚；(key ,values)；
- task会用我们自己定义的聚合函数，比如reduceByKey(_+_)，把所有values进行一对一的累加；聚合出来最终的值。就完成了shuffle。

**shuffle划分**

- shuffle，一定是分为两个stage来完成的。因为这其实是个逆向的过程，不是stage决定shuffle，是shuffle决定stage。
- reduceByKey(_+_)，在某个action触发job的时候，DAGScheduler，会负责划分job为多个stage。划分的依据，就是，如果发现有会触发shuffle操作的算子，比如reduceByKey，就将这个操作的前半部分，以及之前所有的RDD和transformation操作，划分为一个stage；shuffle操作的后半部分，以及后面的，直到action为止的RDD和transformation操作，划分为另外一个stage。
- shuffle前半部分的task在写入数据到磁盘文件之前，都会先写入一个一个的内存缓冲，内存缓冲满溢之后，再spill溢写到磁盘文件中。

### 合并map端输出文件

**不合并map端输出文件情况**

- 前置条件：每个executor有2个cpu core，4个task。task是线程执行的。所以先并行跑2个task，再跑剩下2个task，下一个stage，总共只有2个task。

- 第一个stage，每个task，都会给第二个stage的每个task创建一份map端的输出文件；第二个stage，每个task，会到各个节点上面去，拉取第一个stage每个task输出的，属于自己的那一份文件。

  ![](img\shuffle2.png)

- 实际生产环境的条件：

  ```text
  100个节点（每个节点一个executor）：100个executor
  每个executor：2个cpu core
  总共1000个task：每个executor平均10个task
  每个节点，10个task，每个节点会输出多少份map端文件？10 * 1000=1万个文件
  总共有多少份map端输出文件？100 * 10000 = 100万。
  ```

- shuffle中的写磁盘的操作，基本上就是shuffle中性能消耗最为严重的部分。通过上面的分析，一个普通的生产环境的spark job的一个shuffle环节，会写入磁盘100万个文件。磁盘IO对性能和spark作业执行速度的影响，是极其惊人和吓人的。基本上，spark作业的性能，都消耗在shuffle中了，虽然不只是shuffle的map端输出文件这一个部分，但是这里也是非常大的一个性能消耗点。

**合并map端输出文件**

```java
new SparkConf().set("spark.shuffle.consolidateFiles", "true")
```

开启shuffle map端输出文件合并的机制；默认情况下，是不开启的，就是会发生如上所述的大量map端输出文件的操作，严重影响性能。

![](img\shuffle3.png)

- 第一个stage，同时就运行cpu core个task，比如cpu core是2个，并行运行2个task；每个task都创建下一个stage的task数量个文件；
- 第一个stage，并行运行的2个task执行完以后；就会执行另外两个task；另外2个task不会再重新创建输出文件；而是复用之前的task创建的map端输出文件，将数据写入上一批task的输出文件中。
- 第二个stage，task在拉取数据的时候，就不会去拉取上一个stage每一个task为自己创建的那份输出文件了；而是拉取少量的输出文件，每个输出文件中，可能包含了多个task给自己的map端输出。

提醒一下（map端输出文件合并）：

- 只有并行执行的task会去创建新的输出文件；下一批并行执行的task，就会去复用之前已有的输出文件；但是有一个例外，比如2个task并行在执行，但是此时又启动要执行2个task；那么这个时候的话，就无法去复用刚才的2个task创建的输出文件了；而是还是只能去创建新的输出文件。
- 要实现输出文件的合并的效果，必须是一批task先执行，然后下一批task再执行，才能复用之前的输出文件；负责多批task同时起来执行，还是做不到复用的。

**实际生产环境的条件：**

```text
100个节点（每个节点一个executor）：100个executor
每个executor：2个cpu core
总共1000个task：每个executor平均10个task

每个节点，2个cpu core，有多少份输出文件呢？2 * 1000 = 2000个
总共100个节点，总共创建多少份输出文件呢？100 * 2000 = 20万个文件
```

相比较开启合并机制之前的情况，100万个，map端输出文件，在生产环境中，立减5倍！

**合并map端输出文件，对spark性能的影响**

- map task写入磁盘文件的IO，减少：100万文件 -> 20万文件
- 第二个stage，原本要拉取第一个stage的task数量份文件，1000个task，第二个stage的每个task，都要拉取1000份文件，走网络传输；合并以后，100个节点，每个节点2个cpu core，第二个stage的每个task，主要拉取100 * 2 = 200个文件即可；网络传输的性能消耗是不是也大大减少
- 实际在生产环境中，使用了`spark.shuffle.consolidateFiles`机制以后，实际的性能调优的效果：对于上述的这种生产环境的配置，性能的提升，还是相当的客观的。spark作业，5个小时 -> 2~3个小时。
- 不要小看这个map端输出文件合并机制。实际上，在数据量比较大，已经做了前面的性能调优，executor->cpu core->并行度（task数量），shuffle没调优，shuffle就很糟糕了；大量的map端输出文件的产生。对性能有比较恶劣的影响。
- 这个时候，去开启这个机制，可以很有效的提升性能。

### 调节map端内存缓冲与reduce端内存占比

```sh
spark.shuffle.file.buffer，默认32k
spark.shuffle.memoryFraction，0.2
```

- map端内存缓冲，reduce端内存占比；很多资料、网上视频，都会说，这两个参数，是调节shuffle性能的不二选择，很有效果的样子，实际上，不是这样的。
- 以实际的生产经验来说，这两个参数没有那么重要，往往来说，shuffle的性能不是因为这方面的原因导致的
- 但是，有一点点效果的，broadcast，数据本地化等待时长；这两个shuffle调优的小点，其实也是需要跟其他的大量的小点配合起来使用，一点一点的提升性能，最终很多个性能调优的小点的效果，汇集在一起之后，那么就会有可以看见的还算不错的性能调优的效果。

![](img\map端内存缓冲.png)



**原理介绍**

- 默认情况下，shuffle的map task，输出到磁盘文件的时候，统一都会先写入每个task自己关联的一个内存缓冲区。这个缓冲区大小，默认是32kb。每一次，当内存缓冲区满溢之后，才会进行spill操作，溢写操作，溢写到磁盘文件中去。
- reduce端task，在拉取到数据之后，会用hashmap的数据格式，来对各个key对应的values进行汇聚。针对每个key对应的values，执行我们自定义的聚合函数的代码，比如`_ + _`（把所有values累加起来）
- reduce task，在进行汇聚、聚合等操作的时候，实际上，使用的就是自己对应的executor的内存，executor（jvm进程，堆），默认executor内存中划分给reduce task进行聚合的比例，是0.2。
- 问题来了，因为比例是0.2，所以，理论上，很有可能会出现，拉取过来的数据很多，那么在内存中，放不下；这个时候，默认的行为，就是说，将在内存放不下的数据，都spill（溢写）到磁盘文件中去。

**不调优的情况**

- 默认，map端内存缓冲是每个task，32kb，reduce端聚合内存比例，是0.2，也就是20%。

如果map端的task，处理的数据量比较大，但是呢，你的内存缓冲大小是固定的。可能会出现什么样的情况？

- 每个task就处理320kb，32kb，总共会向磁盘溢写320 / 32 = 10次。
- 每个task处理32000kb，32kb，总共会向磁盘溢写32000 / 32 = 1000次。
- 在map task处理的数据量比较大的情况下，而你的task的内存缓冲默认是比较小的，32kb。可能会造成多次的map端往磁盘文件的spill溢写操作，发生大量的磁盘IO，从而降低性能。
- reduce端聚合内存，占比。默认是0.2。如果数据量比较大，reduce task拉取过来的数据很多，那么就会频繁发生reduce端聚合内存不够用，频繁发生spill操作，溢写到磁盘上去。而且最要命的是，磁盘上溢写的数据量越大，后面在进行聚合操作的时候，很可能会多次读取磁盘中的数据，进行聚合。

默认不调优，在数据量比较大的情况下，可能频繁地发生reduce端的磁盘文件的读写。这两个点之所以放在一起讲，是因为他们俩是有关联的。数据量变大，map端肯定会出点问题；reduce端肯定也会出点问题；出的问题是一样的，都是磁盘IO频繁，变多，影响性能。

**调优后**

在实际生产环境中，我们在什么时候来调节两个参数？

- 看Spark UI，如果你的公司是决定采用standalone模式，那么狠简单，你的spark跑起来，会显示一个Spark UI的地址，4040的端口，进去看，依次点击进去，可以看到，你的每个stage的详情，有哪些executor，有哪些task，每个task的shuffle write和shuffle read的量，shuffle的磁盘和内存，读写的数据量；如果是用的yarn模式来提交，课程最前面，从yarn的界面进去，点击对应的application，进入Spark UI，查看详情。
- 如果发现shuffle 磁盘的write和read，很大。这个时候，就意味着最好调节一些shuffle的参数。进行调优。首先当然是考虑开启map端输出文件合并机制。
- 调节上面说的那两个参数。调节的时候的原则。spark.shuffle.file.buffer，每次扩大一倍，然后看看效果，64，128；spark.shuffle.memoryFraction，每次提高0.1，看看效果。
- 不能调节的太大，太大了以后过犹不及，因为内存资源是有限的，你这里调节的太大了，其他环节的内存使用就会有问题了。
- 调节了以后，效果？map task内存缓冲变大了，减少spill到磁盘文件的次数；reduce端聚合内存变大了，减少spill到磁盘的次数，而且减少了后面聚合读取磁盘文件的数量。

### HashShuffleManager与SortShuffleManager

```spa
spark.shuffle.manager：hash、sort、tungsten-sort（自己实现内存管理）
spark.shuffle.sort.bypassMergeThreshold：200
```

**SortShuffleManager与HashShuffleManager两点不同：**

1. SortShuffleManager会对每个reduce task要处理的数据，进行排序（默认的）。
2. SortShuffleManager会避免像HashShuffleManager那样，默认就去创建多份磁盘文件。一个task，只会写入一个磁盘文件，不同reduce task的数据，用offset来划分界定。

在spark 1.5.x以后，对于shuffle manager又出来了一种新的manager，tungsten-sort（钨丝），钨丝sort shuffle manager。官网上一般说，钨丝sort shuffle manager，效果跟sort shuffle manager是差不多的。但是，唯一的不同之处在于，钨丝manager，是使用了自己实现的一套内存管理机制，性能上有很大的提升， 而且可以避免shuffle过程中产生的大量的OOM，GC，等等内存相关的异常。



**hash、sort、tungsten-sort。如何来选择**

- 需不需要数据默认就让spark给你进行排序？就好像mapreduce，默认就是有按照key的排序。如果不需要的话，其实还是建议搭建就使用最基本的HashShuffleManager，因为最开始就是考虑的是不排序，换取高性能；
- 什么时候需要用sort shuffle manager？如果你需要你的那些数据按key排序了，那么就选择这种吧，而且要注意，reduce task的数量应该是超过200的，这样sort、merge（多个文件合并成一个）的机制，才能生效把。但是这里要注意，你一定要自己考量一下，有没有必要在shuffle的过程中，就做这个事情，毕竟对性能是有影响的。
- 如果你不需要排序，而且你希望你的每个task输出的文件最终是会合并成一份的，你自己认为可以减少性能开销；可以去调节bypassMergeThreshold这个阈值，比如你的reduce task数量是500，默认阈值是200，所以默认还是会进行sort和直接merge的；可以将阈值调节成550，不会进行sort，按照hash的做法，每个reduce task创建一份输出文件，最后合并成一份文件。（一定要提醒大家，这个参数，其实通常不会在生产环境里去使用，也没有经过验证，这样的方式，到底有多少性能的提升）
- 如果你想选用sort based shuffle manager，而且你们公司的spark版本比较高，那么可以考虑去尝试使用tungsten-sort shuffle manager。看看性能的提升与稳定性怎么样。



**总结**

- 在生产环境中，不建议贸然使用第三点和第四点：
- 如果不想要你的数据在shuffle时排序，那么就自己设置一下，用hash shuffle manager。
- 如果你的确是需要你的数据在shuffle时进行排序的，那么就默认不用动，默认就是sort shuffle manager；或者是什么？如果你压根儿不care是否排序这个事儿，那么就默认让他就是sort的。调节一些其他的参数（consolidation机制）。（80%，都是用这种）

```java
spark.shuffle.manager：hash、sort、tungsten-sort

new SparkConf().set("spark.shuffle.manager", "hash")
new SparkConf().set("spark.shuffle.manager", "tungsten-sort")

// 默认就是，new SparkConf().set("spark.shuffle.manager", "sort")
new SparkConf().set("spark.shuffle.sort.bypassMergeThreshold", "550")

```

