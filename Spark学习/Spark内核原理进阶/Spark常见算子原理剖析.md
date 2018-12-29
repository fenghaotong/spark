# Spark常见算子原理剖析

### union算子原理

- 新的rdd，会将旧的两个rdd的partition，原封不动地给挪过来
- 新的rdd的partition的数量，就是旧的两个rdd的partition的数量的综合

![](img\union算子原理.png)

### groupByKey算子原理

- 一般来说，在执行shuffle类的算子的时候，比如groupByKey、reduceByKey、join等。
- 其实算子内部都会隐式地创建几个RDD出来。那些隐式创建的RDD，主要是作为这个操作的一些中间数据的表达，以及作为stage划分的边界。
- 因为有些隐式生成的RDD，可能是ShuffledRDD，dependency就是ShuffleDependency，DAGScheduler的源码，就会将这个RDD作为新的stage的第一个rdd，划分出来。
- groupByKey等shuffle算子，都会创建一些隐式RDD。比如说这里，ShuffledRDD，作为一个shuffle过程中的中间数据的代表。
- 依赖这个ShuffledRDD创建出来一个新的stage（stage1）。ShuffledRDD会去触发shuffle read操作。从上游stage的task所在节点，拉取过来相同的key，做进一步的聚合。
- 对ShuffledRDD中的数据执行一个map类的操作，主要是对每个partition中的数据，都进行一个映射和聚合。这里主要是将每个key对应的数据都聚合到一个Iterator集合中。

![](img\groupByKey算子原理.png)



### reduceByKey算子原理

reduceByKey，看了内部原理之后，跟groupByKey的异同之处，在哪里？

- 不同之处：reduceByKey，多了一个rdd，MapPartitionsRDD，存在于stage0的，主要是代表了进行本地数据归约之后的rdd。所以，要网络传输的数据量，以及磁盘IO等，会减少，性能更高。
- 相同之处：后面进行shuffle read和聚合的过程基本和groupByKey类似。都是ShuffledRDD，去做shuffle read。然后聚合，聚合后的数据就是最终的rdd。

![](img\reduceByKey算子原理.png)

### distinct算子原理

- 首先，自己先给每个值打上一个v2，变成一个tuple
- reduceByKey(...仅仅返回一个value)
- 将去重后的数据，从tuple还原为单值

![](img\distinct算子原理.png)

### cogroup算子原理

- cogroup算子，基础的算子
- cogroup算子是其他很多算子的基础，比如join

![](img\cogroup算子原理.png)

### intersection算子原理

- map，tuple
- cogroup，聚合两个rdd的key
- filter，过滤掉两个集合中任意一个集合为空的key
- map，还原出单key

![](img\intersection算子原理.png)

### join算子原理

- cogroup，聚合两个rdd的key
- flatMap，聚合后的每条数据，都可能返回多条数据，将每个key对应的两个集合的所有元素，做了一个笛卡尔积

![](img\join算子原理.png)

### sortByKey算子原理

- ShuffledRDD，做shuffle read，将相同的key拉到一个partition中来
- mapPartitions，对每个partitions内的key进行全局的排序

![](img\sortByKey算子原理.png)

### cartesian算子原理

![](img\cartesian算子原理.png)

### coalesce算子原理

![](img\coalesce算子原理.png)

### repartition算子原理

- map，附加了前缀，根据要重分区成几个分区，计算出前缀
- shuffle->colesceRDD
- 去掉前缀，得到最终重分区好的RDD

![](img\repartition算子原理.png)

