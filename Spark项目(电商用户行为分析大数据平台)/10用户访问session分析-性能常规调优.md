# 用户访问session分析-性能调优

### 分配更多资源

分配更多资源：性能调优的王道，就是增加和分配更多的资源，性能和速度上的提升，是显而易见的；基本上，在一定范围之内，增加资源与性能的提升，是成正比的；写完了一个复杂的spark作业之后，进行性能调优的时候，首先第一步，我觉得，就是要来调节最优的资源配置；在这个基础之上，如果说你的spark作业，能够分配的资源达到了你的能力范围的顶端之后，无法再分配更多的资源了，公司资源有限；那么才是考虑去做后面的这些性能调优的点。

**分配哪些资源？**

executor、cpu per executor、memory per executor、driver memory

**在哪里分配这些资源？**

在我们在生产环境中，提交spark作业时，用的spark-submit shell脚本，里面调整对应的参数

```sh
/usr/local/spark/bin/spark-submit \
--class cn.spark.sparktest.core.WordCountCluster \
--num-executors 3 \  # 配置executor的数量
--driver-memory 100m \  # 配置driver的内存（影响不大）
--executor-memory 100m \  # 配置每个executor的内存大小
--executor-cores 3 \  # 配置每个executor的cpu core数量
/usr/local/SparkTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
```

**调节到多大，算是最大呢？**

- 第一种，Spark Standalone，公司集群上，搭建了一套Spark集群，你心里应该清楚每台机器还能够给你使用的，大概有多少内存，多少cpu core；那么，设置的时候，就根据这个实际的情况，去调节每个spark作业的资源分配。比如说你的每台机器能够给你使用4G内存，2个cpu core，20台机器；假如启动20个executor；平均每个executor4G内存，2个cpu core。
- 第二种，Yarn。资源队列。资源调度。应该去查看，你的spark作业，要提交到的资源队列，大概有多少资源？500G内存，100个cpu core；启动50个executor；平均每个executor10G内存，2个cpu core。
- 一个原则，你能使用的资源有多大，就尽量去调节到最大的大小（executor的数量，几十个到上百个不等；executor内存；executor cpu core）

**为什么多分配了这些资源以后，性能会得到提升？**

- SparkContext，DAGScheduler，TaskScheduler，会将我们的算子，切割成大量的task，提交到Application的executor上面去执行。
- 增加每个executor的cpu core，也是增加了执行的并行能力。原本20个executor，每个才2个cpu core。能够并行执行的task数量，就是40个task。现在每个executor的cpu core，增加到了5个。能够并行执行的task数量，就是100个task。执行的速度，提升了2.5倍。
- 增加每个executor的内存量。增加了内存量以后，对性能的提升，有两点：
  1. 如果需要对RDD进行cache，那么更多的内存，就可以缓存更多的数据，将更少的数据写入磁盘，甚至不写入磁盘。减少了磁盘IO。
  2. 对于shuffle操作，reduce端，会需要内存来存放拉取的数据并进行聚合。如果内存不够，也会写入磁盘。如果给executor分配更多内存以后，就有更少的数据，需要写入磁盘，甚至不需要写入磁盘。减少了磁盘IO，提升了性能。
  3. 对于task的执行，可能会创建很多对象。如果内存比较小，可能会频繁导致JVM堆内存满了，然后频繁GC，垃圾回收，minor GC和full GC。（速度很慢）。内存加大以后，带来更少的GC，垃圾回收，避免了速度变慢，速度变快了。
- 增加executor：
  1. 如果executor数量比较少，那么，能够并行执行的task数量就比较少，就意味着，我们的Application的并行执行的能力就很弱。
  2. 比如有3个executor，每个executor有2个cpu core，那么同时能够并行执行的task，就是6个。6个执行完以后，再换下一批6个task。
  3. 增加了executor数量以后，那么，就意味着，能够并行执行的task数量，也就变多了。比如原先是6个，现在可能可以并行执行10个，甚至20个，100个。那么并行能力就比之前提升了数倍，数十倍。相应的，性能（执行的速度），也能提升数倍~数十倍。

### 调节并行度

Spark作业，Application，Jobs，action（collect）触发一个job，1个job；每个job拆成多个stage，发生shuffle的时候，会拆分出一个stage，reduceByKey；stage0的task，在最后，执行到reduceByKey的时候，会为每个stage1的task，都创建一份文件（也可能是合并在少量的文件里面）；每个stage1的task，会去各个节点上的各个task创建的属于自己的那一份文件里面，拉取数据；每个stage1的task，拉取到的数据，一定是相同key对应的数据。对相同的key，对应的values，才能去执行我们自定义的function操作（_ + _）

并行度：其实就是指的是，Spark作业中，各个stage的task数量，也就代表了Spark作业的在各个阶段（stage）的并行度。

**如果不调节并行度，导致并行度过低，会怎么样？**

- 假设，现在已经在spark-submit脚本里面，给我们的spark作业分配了足够多的资源，比如50个executor，每个executor有10G内存，每个executor有3个cpu core。基本已经达到了集群或者yarn队列的资源上限。
- task没有设置，或者设置的很少，比如就设置了，100个task。50个executor，每个executor有3个cpu core，也就是说，你的Application任何一个stage运行的时候，都有总数在150个cpu core，可以并行运行。但是你现在，只有100个task，平均分配一下，每个executor分配到2个task，ok，那么同时在运行的task，只有100个，每个executor只会并行运行2个task。每个executor剩下的一个cpu core，就浪费掉了。

资源虽然分配足够了，但是问题是，并行度没有与资源相匹配，导致你分配下去的资源都浪费掉了。

- 合理的并行度的设置，应该是要设置的足够大，大到可以完全合理的利用你的集群资源；比如上面的例子，总共集群有150个cpu core，可以并行运行150个task。那么就应该将你的Application的并行度，至少设置成150，才能完全有效的利用你的集群资源，让150个task，并行执行；而且task增加到150个以后，即可以同时并行运行，还可以让每个task要处理的数据量变少；比如总共150G的数据要处理，如果是100个task，每个task计算1.5G的数据；现在增加到150个task，可以并行运行，而且每个task主要处理1G的数据就可以。

很只要合理设置并行度，就可以完全充分利用你的集群计算资源，并且减少每个task要处理的数据量，最终，就是提升你的整个Spark作业的性能和运行速度。

1. task数量，至少设置成与Spark application的总cpu core数量相同（最理想情况，比如总共150个cpu core，分配了150个task，一起运行，差不多同一时间运行完毕）

2. 官方是推荐，task数量，设置成spark application总cpu core数量的2~3倍，比如150个cpu core，基本要设置task数量为300~500；

   实际情况，与理想情况不同的，有些task会运行的快一点，比如50s就完了，有些task，可能会慢一点，要1分半才运行完，所以如果你的task数量，刚好设置的跟cpu core数量相同，可能还是会导致资源的浪费，因为，比如150个task，10个先运行完了，剩余140个还在运行，但是这个时候，有10个cpu core就空闲出来了，就导致了浪费。那如果task数量设置成cpu core总数的2~3倍，那么一个task运行完了以后，另一个task马上可以补上来，就尽量让cpu core不要空闲，同时也是尽量提升spark作业运行的效率和速度，提升性能。

3. 如何设置一个Spark Application的并行度？

   ```\
   spark.default.parallelism 
   ```

   ```scala
   SparkConf conf = new SparkConf()
     .set("spark.default.parallelism", "500")
   ```

“重剑无锋”：真正有分量的一些技术和点，其实都是看起来比较平凡，但是其实是你每次写完一个spark作业，进入性能调优阶段的时候，应该优先调节的事情，就是这些（大部分时候，可能资源和并行度到位了，spark作业就很快了，几分钟就跑完了）

“炫酷”：数据倾斜（100个spark作业，最多10个会出现真正严重的数据倾斜问题），不能上来就使用；还有JVM调优；



### 重构RDD架构以及RDD持久化

**RDD架构重构与优化**

- 尽量去复用RDD，差不多的RDD，可以抽取称为一个共同的RDD，供后面的RDD计算时，反复使用。

**公共RDD一定要实现持久化**

- 对于要多次计算和使用的公共RDD，一定要进行持久化。
- 持久化，也就是说，将RDD的数据缓存到内存中/磁盘中，（BlockManager），以后无论对这个RDD做多少次计算，那么都是直接取这个RDD的持久化的数据，比如从内存中或者磁盘中，直接提取一份数据。

**持久化，是可以进行序列化的**

- 如果正常将数据持久化在内存中，那么可能会导致内存的占用过大，这样的话，也许，会导致OOM内存溢出。
- 当纯内存无法支撑公共RDD数据完全存放的时候，就优先考虑，使用序列化的方式在纯内存中存储。将RDD的每个partition的数据，序列化成一个大的字节数组，就一个对象；序列化后，大大减少内存的空间占用。
- 序列化的方式，唯一的缺点就是，在获取数据的时候，需要反序列化。
- 如果序列化纯内存方式，还是导致OOM，内存溢出；就只能考虑磁盘的方式，内存+磁盘的普通方式（无序列化）。
- 内存+磁盘，序列化

**为了数据的高可靠性，而且内存充足，可以使用双副本机制，进行持久化**

- 持久化的双副本机制，持久化后的一个副本，因为机器宕机了，副本丢了，就还是得重新计算一次；持久化的每个数据单元，存储一份副本，放在其他节点上面；从而进行容错；一个副本丢了，不用重新计算，还可以使用另外一份副本。
- 这种方式，仅仅针对你的内存资源极度充足

![](img\重构RDD架构以及RDD持久化.png)

### 广播大变量

**不使用广播变量**

![](img\task.png)

默认的，task执行的算子中，使用了外部的变量，每个task都会获取一份变量的副本，有什么缺点呢？在什么情况下，会出现性能上的恶劣的影响呢？

- map，本身是不小，存放数据的一个单位是Entry，还有可能会用链表的格式的来存放Entry链条。所以map是比较消耗内存的数据格式。比如，map是1M。总共，你前面调优都调的特好，资源给的到位，配合着资源，并行度调节的绝对到位，1000个task。大量task的确都在并行运行。
- 这些task里面都用到了占用1M内存的map，那么首先，map会拷贝1000份副本，通过网络传输到各个task中去，给task使用。总计有1G的数据，会通过网络传输。网络传输的开销，不容乐观啊！！！网络传输，也许就会消耗掉你的spark作业运行的总时间的一小部分。
- map副本，传输到了各个task上之后，是要占用内存的。1个map的确不大，1M；1000个map分布在你的集群中，一下子就耗费掉1G的内存。对性能会有什么影响呢？
- 不必要的内存的消耗和占用，就导致了，你在进行RDD持久化到内存，也许就没法完全在内存中放下；就只能写入磁盘，最后导致后续的操作在磁盘IO上消耗性能；
- task在创建对象的时候，也许会发现堆内存放不下所有对象，也许就会导致频繁的垃圾回收器的回收，GC。GC的时候，一定是会导致工作线程停止，也就是导致Spark暂停工作那么一点时间。频繁GC的话，对Spark作业的运行的速度会有相当可观的影响。

**使用广播变量**

![](img\Broadcast.png)

- 广播变量的好处，不是每个task一份变量副本，而是变成每个节点的executor才一份副本。这样的话，就可以让变量产生的副本大大减少。
- 广播变量，初始的时候，就在Drvier上有一份副本。task在运行的时候，想要使用广播变量中的数据，此时首先会在自己本地的Executor对应的BlockManager中，尝试获取变量副本；如果本地没有，那么就从Driver远程拉取变量副本，并保存在本地的BlockManager中；此后这个executor上的task，都会直接使用本地的BlockManager中的副本。executor的BlockManager除了从driver上拉取，也可能从其他节点的BlockManager上拉取变量副本，举例越近越好。
- BlockManager，也许会从远程的Driver上面去获取变量副本；也有可能从距离比较近的其他节点的Executor的BlockManager上去获取

**举例：50个executor，1000个task，一个map10M。**

- 默认情况下，1000个task，1000份副本。10G的数据，网络传输，在集群中，耗费10G的内存资源。如果使用了广播变量。50个execurtor，50个副本。500M的数据，网络传输，而且不一定都是从Driver传输到每个节点，还可能是就近从最近的节点的executor的bockmanager上拉取变量副本，网络传输速度大大增加；500M的内存消耗。
- 10000M，500M，20倍。20倍~以上的网络传输性能消耗的降低；20倍的内存消耗的减少。对性能的提升和影响，还是很客观的。
- 虽然说，不一定会对性能产生决定性的作用。比如运行30分钟的spark作业，可能做了广播变量以后，速度快了2分钟，或者5分钟。但是一点一滴的调优，积少成多。最后还是会有效果的。
- 没有经过任何调优手段的spark作业，16个小时；三板斧下来，就可以到5个小时；然后非常重要的一个调优，影响特别大，shuffle调优，2~3个小时；应用了10个以上的性能调优的技术点，JVM+广播，30分钟。16小时~30分钟。

### 使用Kryo序列化

![](img\Kryo序列化.png)

默认情况下，Spark内部是使用Java的序列化机制，`ObjectOutputStream / ObjectInputStream`，对象输入输出流机制，来进行序列化

- 这种默认序列化机制的好处在于，处理起来比较方便；也不需要我们手动去做什么事情，只是，你在算子里面使用的变量，必须是实现Serializable接口的，可序列化即可。
- 但是缺点在于，默认的序列化机制的效率不高，序列化的速度比较慢；序列化以后的数据，占用的内存空间相对还是比较大。

**可以手动进行序列化格式的优化**

- Spark支持使用Kryo序列化机制。Kryo序列化机制，比默认的Java序列化机制，速度要快，序列化后的数据要更小，大概是Java序列化机制的1/10。
- 所以Kryo序列化优化以后，可以让网络传输的数据变少；在集群中耗费的内存资源大大减少。

**Kryo序列化机制，一旦启用以后，会生效的几个地方：**

- 算子函数中使用到的外部变量，算子函数中使用到的外部变量，使用Kryo以后：优化网络传输的性能，可以优化集群中内存的占用和消耗

>  算子函数中用到了外部变量，会序列化，使用Kryo

- 持久化RDD时进行序列化，`StorageLevel.MEMORY_ONLY_SER，`持久化RDD，优化内存的占用和消耗；持久化RDD占用的内存越少，task执行的时候，创建的对象，就不至于频繁的占满内存，频繁发生GC。

> 当使用了序列化的持久化级别时，在将每个RDD partition序列化成一个大的字节数组时，就会使用Kryo进一步优化序列化的效率和性能

- shuffle：可以优化网络传输的性能

  > 在进行stage间的task的shuffle操作时，节点与节点之间的task会互相大量通过网络拉取和传输文件，此时，这些数据既然通过网络传输，也是可能要序列化的，就会使用Kryo

**实现**

```java
SparkConf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
```

- 首先第一步，在SparkConf中设置一个属性

  Kryo之所以没有被作为默认的序列化类库的原因，就要出现了：主要是因为Kryo要求，如果要达到它的最佳性能的话，那么就一定要注册你自定义的类（比如，你的算子函数中使用到了外部自定义类型的对象变量，这时，就要求必须注册你的类，否则Kryo达不到最佳性能）。

  第二步，注册你使用到的，需要通过Kryo序列化的，一些自定义类，`SparkConf.registerKryoClasses()`

项目中的使用：

```java
.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
.registerKryoClasses(new Class[]{CategorySortKey.class})
```

### fastutil优化数据格式

**fastutil介绍**

- fastutil是扩展了Java标准集合框架（Map、List、Set；HashMap、ArrayList、HashSet）的类库，提供了特殊类型的map、set、list和queue；
- fastutil能够提供更小的内存占用，更快的存取速度；我们使用fastutil提供的集合类，来替代自己平时使用的JDK的原生的Map、List、Set，好处在于，fastutil集合类，可以减小内存的占用，并且在进行集合的遍历、根据索引（或者key）获取元素的值和设置元素的值的时候，提供更快的存取速度；
- fastutil也提供了64位的array、set和list，以及高性能快速的，以及实用的IO类，来处理二进制和文本类型的文件；
- fastutil的每一种集合类型，都实现了对应的Java中的标准接口（比如fastutil的map，实现了Java的Map接口），因此可以直接放入已有系统的任何代码中。
- fastutil还提供了一些JDK标准类库中没有的额外功能（比如双向迭代器）。
- fastutil除了对象和原始类型为元素的集合，fastutil也提供引用类型的支持，但是对引用类型是使用等于号（=）进行比较的，而不是equals()方法。
- fastutil尽量提供了在任何场景下都是速度最快的集合类库。

**Spark中应用fastutil的场景：**

- 如果算子函数使用了外部变量；那么第一，你可以使用Broadcast广播变量优化；第二，可以使用Kryo序列化类库，提升序列化性能和效率；第三，如果外部变量是某种比较大的集合，那么可以考虑使用fastutil改写外部变量，首先从源头上就减少内存的占用，通过广播变量进一步减少内存占用，再通过Kryo序列化类库进一步减少内存占用。
- 在你的算子函数里，也就是task要执行的计算逻辑里面，如果有逻辑中，出现，要创建比较大的Map、List等集合，可能会占用较大的内存空间，而且可能涉及到消耗性能的遍历、存取等集合操作；那么此时，可以考虑将这些集合类型使用fastutil类库重写，使用了fastutil集合类以后，就可以在一定程度上，减少task创建出来的集合类型的内存占用。避免executor内存频繁占满，频繁唤起GC，导致性能下降。

**fastutil调优的说明**

- fastutil其实没有那么强大，也不会跟官网上说的效果那么一鸣惊人。广播变量、Kryo序列化类库、fastutil，对于性能来说，提高不会太多。分配资源、并行度、RDD架构与持久化，是主要的调优关键；broadcast、kryo、fastutil，知识辅助。
- 比如说，spark作业，经过之前一些调优以后，大概30分钟运行完，现在加上broadcast、kryo、fastutil，也许就是优化到29分钟运行完、或者更好一点，也许就是28分钟、25分钟。
- shuffle调优，15分钟；groupByKey用reduceByKey改写，执行本地聚合，也许10分钟；跟公司申请更多的资源，比如资源更大的YARN队列，1分钟。

**fastutil的使用：**

- 第一步：在pom.xml中引用fastutil的包

```xml
<dependency>
   <groupId>fastutil</groupId>
   <artifactId>fastutil</artifactId>
   <version>5.0.9</version>
</dependency>
```

>  速度比较慢，可能是从国外的网去拉取jar包，可能要等待5分钟，甚至几十分钟，不等

```java
List<Integer> => IntList
```

- 基本都是类似于IntList的格式，前缀就是集合的元素类型；特殊的就是Map，Int2IntMap，代表了key-value映射的元素类型。除此之外，刚才也看到了，还支持object、reference。

### 调节数据本地化等待时长

![](img\本地化等待时长.png)

**本地化等待时长原理**

- Spark在Driver上，对Application的每一个stage的task，进行分配之前，都会计算出每个task要计算的是哪个分片数据，RDD的某个partition；Spark的task分配算法，优先，会希望每个task正好分配到它要计算的数据所在的节点，这样的话，就不用在网络间传输数据；
- 但是，有时，可能task没有机会分配到它的数据所在的节点，为什么呢？可能那个节点的计算资源和计算能力都满了；所以呢，这种时候，通常来说，Spark会等待一段时间，默认情况下是3s钟（不是绝对的，还有很多种情况，对不同的本地化级别，都会去等待），到最后，实在是等待不了了，就会选择一个比较差的本地化级别，比如说，将task分配到靠它要计算的数据所在节点，比较近的一个节点，然后进行计算。
- 第二种情况是，肯定是要发生数据传输，task会通过其所在节点的BlockManager来获取数据，BlockManager发现自己本地没有数据，会通过一个getRemote()方法，通过TransferService（网络数据传输组件）从数据所在节点的BlockManager中，获取数据，通过网络传输回task所在节点。
- 当然不最好不要第二种情况。最好的，当然是task和数据在一个节点上，直接从本地executor的BlockManager中获取数据，纯内存，或者带一点磁盘IO；如果要通过网络传输数据的话，那么实在是，性能肯定会下降的，大量网络传输，以及磁盘IO，都是性能的杀手。

**spark本地化级别**

- PROCESS_LOCAL：进程本地化，代码和数据在同一个进程中，也就是在同一个executor中；计算数据的task由executor执行，数据在executor的BlockManager中；性能最好
- NODE_LOCAL：节点本地化，代码和数据在同一个节点中；比如说，数据作为一个HDFS block块，就在节点上，而task在节点上某个executor中运行；或者是，数据和task在一个节点上的不同executor中；数据需要在进程间进行传输
- NO_PREF：对于task来说，数据从哪里获取都一样，没有好坏之分
- RACK_LOCAL：机架本地化，数据和task在一个机架的两个节点上；数据需要通过网络在节点之间进行传输
- ANY：数据和task可能在集群中的任何地方，而且不在一个机架中，性能最差

> `spark.locality.wait`，默认是3s

**什么时候要调节这个参数**

- 观察日志，spark作业的运行日志，在测试的时候，先用client模式，在本地就直接可以看到比较全的日志。日志里面会显示，starting task。。。，PROCESS LOCAL、NODE LOCAL，观察大部分task的数据本地化级别
- 如果大多都是PROCESS_LOCAL，那就不用调节了，如果是发现，好多的级别都是NODE_LOCAL、ANY，那么最好就去调节一下数据本化的等待时长
- 调节完，应该是要反复调节，每次调节完以后，再来运行，观察日志，看看大部分的task的本地化级别有没有提升；看看，整个spark作业的运行时间有没有缩短



你别本末倒置，本地化级别倒是提升了，但是因为大量的等待时长，spark作业的运行时间反而增加了，那就还是不要调节了

**怎么调节？**

- `spark.locality.wait`，默认是3s；6s，10s

- 默认情况下，下面3个的等待时长，都是3s

  ```scala
  spark.locality.wait.process
  spark.locality.wait.node
  spark.locality.wait.rack
  
  new SparkConf()
    .set("spark.locality.wait", "10")
  ```


