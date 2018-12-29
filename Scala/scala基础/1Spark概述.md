#  Spark概述

Spark使用Spark RDD、Spark SQL、Spark Streaming、MLib、GraphX就解决了大数据领域中离线批处理、交互式查询、流失计算、机器学习、图计算等常见的任务。

集成Hadoop：Hadoop的HDFS、Hive、HBase负责存储，YARN负责资源调度，Spark负责复杂计算。

Spark相较于MepReduce速度快的主要原因，MapReduce的计算模型太死板，必须是map-reduce模式。

Spark SQL和Hive：Spark SQL替代的是Hive的查询引擎，而不是全部

SparkStreaming和Storm：都用于实时流计算，如果对实时性要求较高则使用Storm，如果在几秒内实时使用SparkStreaming。

Hadoop + Spark



- 如果能够学扎实基础课程，以及Spark核心编程，那么可以称之为Spark入门级别的水平。

- 如果能够学扎实基础课程、Spark核心编程，以及Spark SQL和Spark Streaming的所有功能使用，并熟练掌握，那么可以称之为熟悉Spark的水平。

- b如果能够学精通本课程所有的内容，包括基础、各组件功能使用、Spark内核原理、Spark内核源码、Spark性能调优、Spark SQL原理和性能调优、Spark Streaming原理和性能调优，那么可以称之为精通Spark的水平。