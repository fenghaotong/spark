# shuffle性能优化

![没有开启consolidation机制的性能低下的原理剖析](img\没有开启consolidation机制的性能低下的原理剖析.png)

![](img\开启consolidation机制之后对磁盘io性能的提升的原理.png)

```scala
new SparkConf().set("spark.shuffle.consolidateFiles", "true")
```

- spark.shuffle.consolidateFiles：是否开启shuffle block file的合并，默认为false
- spark.reducer.maxSizeInFlight：reduce task的拉取缓存，默认48m
- spark.shuffle.file.buffer：map task的写磁盘缓存，默认32k
- spark.shuffle.io.maxRetries：拉取失败的最大重试次数，默认3次
- spark.shuffle.io.retryWait：拉取失败的重试间隔，默认5s
- spark.shuffle.memoryFraction：用于reduce端聚合的内存比例，默认0.2，超过比例就会溢出到磁盘上