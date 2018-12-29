# 易用性：标准化SQL支持以及更合理的API

**标准化SQL支持以及更合理的API**

- Spark最引以为豪的几个特点就是简单、直观、表达性好。Spark 2.0为了继续加强这几个特点，做了两件事情：1、提供标准化的SQL支持；2、统一了Dataframe和Dataset两套API。
- 在标准化SQL支持方面，引入了新的ANSI-SQL解析器，提供标准化SQL的解析功能，而且还提供了子查询的支持。Spark现在可以运行完整的99个TPC-DS查询，这就要求Spark包含大多数SQL 2003标准的特性。这么做的好处在于，SQL一直是大数据应用领域的一个最广泛接受的标准，比如说Hadoop，做大数据的企业90%的时间都在用Hive，写SQL做各种大数据的统计和分析。因此Spark SQL提升对SQL的支持，可以大幅度减少用户将应用从其他技术（比如Oracle、Hive等）迁移过来的成本。

**统一Dataframe和Dataset API**

- 从Spark 2.0开始，Dataframe就只是Dataset[Row]的一个别名，不再是一个单独的类了。无论是typed方法（map、filter、groupByKey等）还是untyped方法（select、groupBy等），都通过Dataset来提供。而且Dataset API将成为Spark的新一代流式计算框架——structured streaming的底层计算引擎。但是由于Python和R这两个语言都不具备compile-time type-safety的特性，所以就没有引入Dataset API，所以这两种语言中的主要编程接口还是Dataframe。

**SparkSession**

- SparkSession是新的Spark上下文以及入口，用于合并SQLContext和HiveContext，并替代它们。因为以前提供了SQLContext和HiveContext两种上下文入口，因此用户有时会有些迷惑，到底该使用哪个接口。现在好了，只需要使用一个统一的SparkSession即可。但是为了向后兼容性，SQLContext和HiveContext还是保留下来了。

**新版本Accumulator API**

- Spark 2.0提供了新版本的Accumulator，提供了各种方便的方法，比如说直接通过一个方法的调用，就可以创建各种primitive data type（原始数据类型，int、long、double）的Accumulator。并且在spark web ui上也支持查看spark application的accumulator，性能也得到了提升。老的Accumulator API还保留着，主要是为了向后兼容性。

**基于Dataframe/Dataset的Spark MLlib**

- Spark 2.0中，spark.ml包下的机器学习API，主要是基于Dataframe/Dataset来实现的，未来将会成为主要发展的API接口。原先老的基于RDD的spark.mllib包的机器学习API还会保留着，为了向后兼容性，但是未来主要会基于spark.ml包下的接口来进行开发。而且用户使用基于Dataframe/Dataset的新API，还能够对算法模型和pipeline进行持久化保存以及加载。

**SparkR中的分布式机器学习算法以及UDF函数**

- Spark 2.0中，为SparkR提供了分布式的机器学习算法，包括经典的Generalized
  Linear Model，朴素贝叶斯，Survival Regression，K-means等。此外SparkR还支持用户自定义的函数，即UDF。