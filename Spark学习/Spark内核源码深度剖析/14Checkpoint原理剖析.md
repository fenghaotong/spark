# Checkpoint原理剖析

## 什么是Checkpoint

- Checkpoint，是Spark提供的一个比较高级的功能。有的时候啊，比如说，我们的Spark应用程序，特别的复杂，然后呢，从初始的RDD开始，到最后整个应用程序完成，有非常多的步骤，比如超过20个transformation操作。而且呢，整个应用运行的时间也特别长，比如通常要运行1~5个小时。
- 在上述情况下，就比较适合使用checkpoint功能。因为，对于特别复杂的Spark应用，有很高的风险，会出现某个要反复使用的RDD，因为节点的故障，虽然之前持久化过，但是还是导致数据丢失了。那么也就是说，出现失败的时候，没有容错机制，所以当后面的transformation操作，又要使用到该RDD时，就会发现数据丢失了（CacheManager），此时如果没有进行容错处理的话，那么可能就又要重新计算一次数据。
- 简而言之，针对上述情况，整个Spark应用程序的容错性很差。就可以使用checkponit功能。

## Checkpoint功能

- checkpoint功能是什么意思？checkpoint就是说，对于一个复杂的RDD chain，我们如果担心中间某些关键的，在后面会反复几次使用的RDD，可能会因为节点的故障，导致持久化数据的丢失，那么就可以针对该RDD格外启动checkpoint机制，实现容错和高可用。
- checkpoint，就是说，首先呢，要调用SparkContext的setCheckpointDir()方法，设置一个容错的文件系统的目录，比如说HDFS；然后，对RDD调用调用checkpoint()方法。之后，在RDD所处的job运行结束之后，会启动一个单独的job，来将checkpoint过的RDD的数据写入之前设置的文件系统，进行高可用、容错的类持久化操作。
- 那么此时，即使在后面使用RDD时，它的持久化的数据，不小心丢失了，但是还是可以从它的checkpoint文件中直接读取其数据，而不需要重新计算。（CacheManager）

```scala
SparkContext.setCheckpointDir()
RDD.checkpoint()
```

- Checkpoint与持久化的不同：lineage的改变
- RDD.iterator()：读取checkpoint数据
- 给要checkpoint的RDD，先进行persist(StorageLevel.DISK_ONLY)



![](img\Checkpoint原理剖析.png)

