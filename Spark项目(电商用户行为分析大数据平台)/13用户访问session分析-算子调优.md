# 用户访问session分析-算子调优

### MapPartitions提升Map类操作性能

spark中，最基本的原则，就是每个task处理一个RDD的partition。

**MapPartitions操作的优点：**

- 如果是普通的map，比如一个partition中有1万条数据；ok，那么你的function要执行和计算1万次。
- 但是，使用MapPartitions操作之后，一个task仅仅会执行一次function，function一次接收所有的partition数据。只要执行一次就可以了，性能比较高。

**MapPartitions的缺点**

- 如果是普通的map操作，一次function的执行就处理一条数据；那么如果内存不够用的情况下，比如处理了1千条数据了，那么这个时候内存不够了，那么就可以将已经处理完的1千条数据从内存里面垃圾回收掉，或者用其他方法，腾出空间来吧。
- 所以说普通的map操作通常不会导致内存的OOM异常。
- 但是MapPartitions操作，对于大量数据来说，比如甚至一个partition，100万数据，一次传入一个function以后，那么可能一下子内存不够，但是又没有办法去腾出内存空间来，可能就OOM，内存溢出。

**什么时候用MapPartitions**

- 数据量不是特别大的时候，都可以用这种MapPartitions系列操作，性能还是非常不错的，是有提升的。
- 但是也有过出问题的经验，MapPartitions只要一用，直接OOM，内存溢出，崩溃。
- 在项目中，自己先去估算一下RDD的数据量，以及每个partition的量，还有自己分配给每个executor的内存资源。看看一下子内存容纳所有的partition数据，行不行。如果行，可以试一下，能跑通就好。性能肯定有提升。

### filter过后使用coalesce减少分区数量

默认情况下，经过了这种filter之后，RDD中的每个partition的数据量，可能都不太一样了。（原本每个partition的数据量可能是差不多的）

**问题：**

- 每个partition数据量变少了，但是在后面进行处理的时候，还是要跟partition数量一样数量的task，来进行处理；有点浪费task计算资源。
- 每个partition的数据量不一样，会导致后面的每个task处理每个partition的时候，每个task要处理的数据量就不同，这个时候很容易发生什么问题？数据倾斜。。。。
- 比如说，第二个partition的数据量才100；但是第三个partition的数据量是900；那么在后面的task处理逻辑一样的情况下，不同的task要处理的数据量可能差别达到了9倍，甚至10倍以上；同样也就导致了速度的差别在9倍，甚至10倍以上。这样的话呢，就会导致有些task运行的速度很快；有些task运行的速度很慢。这，就是数据倾斜。

**解决方案**

- 针对第一个问题，我们希望可以进行partition的压缩吧，因为数据量变少了，那么partition其实也完全可以对应的变少。比如原来是4个partition，现在完全可以变成2个partition。那么就只要用后面的2个task来处理即可。就不会造成task计算资源的浪费。（不必要，针对只有一点点数据的partition，还去启动一个task来计算）
- 针对第二个问题，其实解决方案跟第一个问题是一样的；也是去压缩partition，尽量让每个partition的数据量差不多。那么这样的话，后面的task分配到的partition的数据量也就差不多。不会造成有的task运行速度特别慢，有的task运行速度特别快。避免了数据倾斜的问题。

**如何实现**

- 使用coalesce算子
- 主要就是用于在filter操作之后，针对每个partition的数据量各不相同的情况，来压缩partition的数量。减少partition的数量，而且让每个partition的数据量都尽量均匀紧凑，从而便于后面的task进行计算操作，在某种程度上，能够一定程度的提升性能。

### 使用foreachPartition优化写数据库性能

![](img\foreach的写库原理.png)

**默认的foreach的性能缺陷**

- 首先，对于每条数据，都要单独去调用一次function，task为每个数据，都要去执行一次function函数。如果100万条数据，（一个partition），调用100万次。性能比较差。
- 另外一个非常非常重要的一点，如果每个数据，你都去创建一个数据库连接的话，那么你就得创建100万次数据库连接。但是要注意的是，数据库连接的创建和销毁，都是非常非常消耗性能的。虽然我们之前已经用了数据库连接池，只是创建了固定数量的数据库连接。你还是得多次通过数据库连接，往数据库（MySQL）发送一条SQL语句，然后MySQL需要去执行这条SQL语句。如果有100万条数据，那么就是100万次发送SQL语句。

**使用foreachPartition算子的好处**

foreachPartition，在生产环境中，通常来说，都使用foreachPartition来写数据库的

1. 对于我们写的function函数，就调用一次，一次传入一个partition所有的数据
2. 主要创建或者获取一个数据库连接就可以
3. 只要向数据库发送一次SQL语句和多组参数即可

在实际生产环境中，都是使用foreachPartition操作；但是有个问题，跟mapPartitions操作一样，如果一个partition的数量真的特别特别大，比如真的是100万，那基本上就不太靠谱了。一下子进来，很有可能会发生OOM，内存溢出的问题。

### 使用repartition解决Spark SQL低并行度的性能问题

**设置并行度**

- spark.default.parallelism
- textFile()，传入第二个参数，指定partition数量（比较少用）

**设置的这个并行度，在哪些情况下会生效？哪些情况下，不会生效？**

- 如果没有使用Spark SQL（DataFrame），那么你整个spark application默认所有stage的并行度都是你设置的那个参数。（除非你使用coalesce算子缩减过partition数量）
- 问题来了，Spark SQL，用了。用Spark SQL的那个stage的并行度，你没法自己指定。Spark SQL自己会默认根据hive表对应的hdfs文件的block，自动设置Spark SQL查询所在的那个stage的并行度。通过`spark.default.parallelism`参数指定的并行度，只会在没有Spark SQL的stage中生效。
- 比如第一个stage，用了Spark SQL从hive表中查询出了一些数据，然后做了一些transformation操作，接着做了一个shuffle操作（groupByKey）；下一个stage，在shuffle操作之后，做了一些transformation操作。hive表，对应了一个hdfs文件，有20个block；你自己设置了spark.default.parallelism参数为100。
- 第一个stage的并行度，是不受你的控制的，就只有20个task；第二个stage，才会变成你自己设置的那个并行度，100。

**问题**

- Spark SQL默认情况下，它的那个并行度，咱们没法设置。可能导致的问题，也许没什么问题，也许很有问题。Spark SQL所在的那个stage中，后面的那些transformation操作，可能会有非常复杂的业务逻辑，甚至说复杂的算法。如果你的Spark SQL默认把task数量设置的很少，20个，然后每个task要处理为数不少的数据量，然后还要执行特别复杂的算法。
- 这个时候，就会导致第一个stage的速度，特别慢。第二个stage，1000个task，刷刷刷，非常快。

**解决**

- repartition算子，用Spark SQL这一步的并行度和task数量，肯定是没有办法去改变了。但是呢，可以将Spark SQL查询出来的RDD，使用repartition算子，去重新进行分区，此时可以分区成多个partition，比如从20个partition，分区成100个。
- 然后，从repartition以后的RDD，再往后，并行度和task数量，就会按照你预期的来了。就可以避免跟Spark SQL绑定在一个stage中的算子，只能使用少量的task去处理大量数据以及复杂的算法逻辑。

### reduceByKey本地聚合介绍

- reduceByKey，相较于普通的shuffle操作（比如groupByKey），它的一个特点，就是说，会进行map端的本地聚合。
- 对map端给下个stage每个task创建的输出文件中，写数据之前，就会进行本地的combiner操作，也就是说对每一个key，对应的values，都会执行你的算子函数`（_ + _）`

**用reduceByKey对性能的提升：**

1. 在本地进行聚合以后，在map端的数据量就变少了，减少磁盘IO。而且可以减少磁盘空间的占用。
2. 下一个stage，拉取数据的量，也就变少了。减少网络的数据传输的性能消耗。
3. 在reduce端进行数据缓存的内存占用变少了。reduce端，要进行聚合的数据量也变少了。

**总结：reduceByKey在什么情况下使用呢？**

1. 非常普通的，比如说，就是要实现类似于wordcount程序一样的，对每个key对应的值，进行某种数据公式或者算法的计算（累加、类乘）
2. 对于一些类似于要对每个key进行一些字符串拼接的这种较为复杂的操作，可以自己衡量一下，其实有时，也是可以使用reduceByKey来实现的。但是不太好实现。如果真能够实现出来，对性能绝对是有帮助的。（shuffle基本上就占了整个spark作业的90%以上的性能消耗，主要能对shuffle进行一定的调优，都是有价值的）