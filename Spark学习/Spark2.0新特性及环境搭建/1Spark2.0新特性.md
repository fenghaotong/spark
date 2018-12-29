# Spark2.0新特性

### Spark Core&Spark SQL 



**API**

- **dataframe与dataset统一，dataframe只是dataset[Row]的类型别名**
- **SparkSession：统一SQLContext和HiveContext，新的上下文入口**
- 为SparkSession开发的一种新的流式调用的configuration api
-  accumulator功能增强：便捷api、web ui支持、性能更高
- dataset的增强聚合api



**SQL**

- **支持sql 2003标准**
- 支持ansi-sql和hive ql的sql parser
- 支持ddl命令
- **支持子查询：in/not in、exists/not exists**



**new feature**

- 支持csv文件
- **支持缓存和程序运行的堆外内存管理**
- 支持hive风格的bucket表
- 支持近似概要统计，包括近似分位数、布隆过滤器、最小略图



**性能**

- **通过whole-stage code generation技术将spark sql和dataset的性能提升2~10倍**
- **通过vectorization技术提升parquet文件的扫描吞吐量**
-  提升orc文件的读写性能
- 提升catalyst查询优化器的性能
- 通过native实现方式提升窗口函数的性能
- 对某些数据源进行自动文件合并

### Spark MLlib

- **spark mllib未来将主要基于dataset api来实现，基于rdd的api转为维护阶段**
- 基于dataframe的api，支持持久化保存和加载模型和pipeline
- 基于dataframe的api，支持更多算法，包括二分kmeans、高斯混合、maxabsscaler等
- spark R支持mllib算法，包括线性回归、朴素贝叶斯、kmeans、多元回归等
- pyspark支持更多mllib算法，包括LDA、高斯混合、泛化线性回顾等
- 基于dataframe的api，向量和矩阵使用性能更高的序列化机制

### Spark Streaming

- **发布测试版的structured streaming**
  - **基于spark sql和catalyst引擎构建**
  - **支持使用dataframe风格的api进行流式计算操作**
  - **catalyst引擎能够对执行计划进行优化**
- 基于dstream的api支持kafka 0.10版本

### 依赖管理、打包和操作

- 不再需要在生产环境部署时打包fat jar，可以使用provided风格
- 完全移除了对akka的依赖
- mesos粗粒度模式下，支持启动多个executor
- 支持kryo 3.0版本
- **使用scala 2.11替代了scala 2.10**

### 移除的功能

- bagel模块

- **对hadoop 2.1以及之前版本的支持**

- 闭包序列化配置的支持

- HTTPBroadcast支持

- 基于TTL模式的元数据清理支持

- 半私有的org.apache.spark.Logging的使用支持

- SparkContext.metricsSystem API

- 与tachyon的面向block的整合支持

- **spark 1.x中标识为过期的所有api**

- python dataframe中返回rdd的方法

- 使用很少的streaming数据源支持：twitter、akka、MQTT、ZeroMQ

- **hash-based shuffle manager**

- standalone master的历史数据支持功能

- **dataframe不再是一个类，而是dataset[Row]的类型别名**

### 变化的机制

- **要求基于scala 2.11版本进行开发，而不是scala 2.10版本**
- SQL中的浮点类型，使用decimal类型来表示，而不是double类型
-  **kryo版本升级到了3.0**
-  java的flatMap和mapPartitions方法，从iterable类型转变为iterator类型
-  java的countByKey返回`<K,Long>`类型，而不是`<K,Object>`类型
-  写parquet文件时，summary文件默认不会写了，需要开启参数来启用
-  spark mllib中，基于dataframe的api完全依赖于自己，不再依赖mllib包

### 过期的API

- mesos的细粒度模式
- **java 7支持标识为过期，可能2.x未来版本会移除支持**
- python 2.6的支持