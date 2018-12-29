# reduceByKey和groupByKey

```scala
val counts = pairs.reduceByKey(_ + _)

val counts = pairs.groupByKey().map(wordCounts => (wordCounts._1, wordCounts._2.sum))

```

- 如果能用reduceByKey，那就用reduceByKey，因为它会在map端，先进行本地combine，可以大大减少要传输到reduce端的数据量，减小网络传输的开销。
- 只有在reduceByKey处理不了时，才用groupByKey().map()来替代。

![](img\groupByKey原理.png)

![reduceByKey原理](img\reduceByKey原理.png)