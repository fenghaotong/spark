# StreamingContext详解

- 有两种创建StreamingContext的方式：

  ```scala
  val conf = new SparkConf().setAppName(appName).setMaster(master);
  
  val ssc = new StreamingContext(conf, Seconds(1));
  ```

- StreamingContext，还可以使用已有的SparkContext来创建

  ```scala
  val sc = new SparkContext(conf)
  
  val ssc = new StreamingContext(sc, Seconds(1));
  ```

- appName，是用来在Spark UI上显示的应用名称。master，是一个`Spark、Mesos或者Yarn集群的URL`，或者是`local[*]`。

- batch interval可以根据你的应用程序的延迟要求以及可用的集群资源情况来设置。

**一个StreamingContext定义之后，必须做以下几件事情：**

1. 通过创建输入DStream来创建输入数据源。
2. 通过对DStream定义transformation和output算子操作，来定义实时计算逻辑。
3. 调用StreamingContext的start()方法，来开始实时处理数据。
4. 调用StreamingContext的awaitTermination()方法，来等待应用程序的终止。可以使用CTRL+C手动停止，或者就是让它持续不断的运行进行计算。
5. 也可以通过调用StreamingContext的stop()方法，来停止应用程序。

**需要注意的要点：**

1. 只要一个StreamingContext启动之后，就不能再往其中添加任何计算逻辑了。比如执行start()方法之后，还给某个DStream执行一个算子。
2. 一个StreamingContext停止之后，是肯定不能够重启的。调用stop()之后，不能再调用start()
3. 一个JVM同时只能有一个StreamingContext启动。在你的应用程序中，不能创建两个StreamingContext。
4. 调用stop()方法时，会同时停止内部的SparkContext，如果不希望如此，还希望后面继续使用SparkContext创建其他类型的Context，比如SQLContext，那么就用stop(false)。
5. 一个SparkContext可以创建多个StreamingContext，只要上一个先用stop(false)停止，再创建下一个即可。