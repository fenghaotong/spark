# output mode、sink以及foreach sink详解

### output操作

**output操作**

定义好了各种计算操作之后，就需要启动这个应用。此时就需要使用DataStreamWriter，通过spark.writeStream()方法返回。此时需要指定以下一些信

- output sink的一些细节：数据格式、位置等。
- output mode：以哪种方式将result table的数据写入sink。
- query name：指定查询的标识。
- trigger interval：如果不指定，那么默认就会尽可能快速地处理数据，只要之前的数据处理完，就会立即处理下一条数据。如果上一个数据还没处理完，而这一个trigger也错过了，那么会一起放入下一个trigger再处理。
- checkpoint地址：对于某些sink，可以做到一次且仅一次的语义，此时需要指定一个目录，进而可以将一些元信息写入其中。一般会是类似hdfs上的容错目录。

**output mode**

目前仅仅支持两种output mode

- append mode：仅适用于不包含聚合操作的查询。
- complete mode：仅适用于包含聚合操作的查询。

**[output sink](http://spark.apache.org/docs/2.3.0/structured-streaming-programming-guide.html#output-sinks)**

目前有一些内置支持的sink

- file sink：在spark 2.0中，仅仅支持parquet文件，以及append模式

- foreach sink

- console sink：仅供调试

- memory sink：仅供调试

  ![](img\outputsink.png)

**foreach sink详解**

使用foreach sink时，我们需要自定义ForeachWriter，并且自定义处理每条数据的业务逻辑。每次trigger发生后，根据output mode需要写入sink的数据，就会传递给ForeachWriter来进行处理。使用如下方式来定义ForeachWriter：

```scala
datasetOfString.write.foreach(new ForeachWriter[String] {
  def open(partitionId: Long, version: Long): Boolean = {
    // open connection
  }
  def process(record: String) = {
    // write string to connection
  }
  def close(errorOrNull: Throwable): Unit = {
    // close the connection
  }
})

```

需要有如下一些注意点：

- ForeachWriter必须支持序列化，因为该对象会被序列化后发送到executor上去执行。
- open、process和close这三个方法都会给executor调用。
- ForeachWriter所有的初始化方法，必须创建数据库连接，开启一个事务，打开一个IO流等等，都必须在open方法中完成。必须注意，如果在ForeachWriter的构造函数中进行初始化，那么这些操作都是在driver上发生的。
- open中有两个参数，version和partition，可以唯一标识一批需要处理的数据。每次发生一次trigger，version就会自增长一次。partition是要处理的结果数据的分区号。因为output操作是分布式执行的，会分布在多个executor上并行执行。
- open可以使用version和partition来决定，是否要处理这一批数据。此时可以选择返回true或false。如果返回false，那么process不会被调用。举个例子来说，有些partition的数据可能已经被持久化了，而另外一些partiton的处理操作由于失败被重试，此时之前已经被持久化的数据可以不再次进行持久化，避免重复计算。
- close方法中，需要处理一些异常，以及一些资源的释放。



**[streaming query](http://spark.apache.org/docs/2.3.0/structured-streaming-programming-guide.html#managing-streaming-queries)**

```scala
val query = df.writeStream.format("console").start()   // get the query object
query.id          // get the unique identifier of the running query
query.name        // get the name of the auto-generated or user-specified name
query.explain()   // print detailed explanations of the query
query.stop()      // stop the query 
query.awaitTermination()   // block until query is terminated, with stop() or with error
query.exception()    // the exception if the query has been terminated with error
query.sourceStatus()  // progress information about data has been read from the input sources
query.sinkStatus()   // progress information about data written to the output sink


```

```SCALA
val spark: SparkSession = ...
spark.streams.active    // get the list of currently active streaming queries
spark.streams.get(id)   // get a query object by its unique id
spark.streams.awaitAnyTermination()   // block until any one of them terminates
```

**[基于checkpoint的容错机制](http://spark.apache.org/docs/2.3.0/structured-streaming-programming-guide.html#recovering-from-failures-with-checkpointing)**

如果实时计算作业遇到了某个错误挂掉了，那么我们可以配置容错机制让它自动重启，同时继续之前的进度运行下去。这是通过checkpoint和wal机制完成的。可以给query配置一个checkpoint
location，接着query会将所有的元信息（比如每个trigger消费的offset范围、至今为止的聚合结果数据），写入checkpoint目录。