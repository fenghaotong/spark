# 数据源Parquet

## 使用编程方式加载数据

Parquet是面向分析型业务的列式存储格式，由Twitter和Cloudera合作开发，2015年5月从Apache的孵化器里毕业成为Apache顶级项目，最新的版本是1.8.0。

列式存储和行式存储相比有哪些优势呢？

1. 可以跳过不符合条件的数据，只读取需要的数据，降低IO数据量。 

2. 压缩编码可以降低磁盘存储空间。由于同一列的数据类型是一样的，可以使用更高效的压缩编码（例如Run Length Encoding和Delta Encoding）进一步节约存储空间。 

3. 只读取需要的列，支持向量运算，能够获取更好的扫描性能。 

[Java版本](src/java/ParquetLoadData.java)

[Scala版本](src/scala/ParquetLoadData.scala)

## 自动分区推断

- 表分区是一种常见的优化方式，比如Hive中就提供了表分区的特性。在一个分区表中，不同分区的数据通常存储在不同的目录中，分区列的值通常就包含在了分区目录的目录名中。Spark SQL中的Parquet数据源，支持自动根据目录名推断出分区信息。例如，如果将人口数据存储在分区表中，并且使用性别和国家作为分区列。那么目录结构可能如下所示：
- 如果将/tableName传入SQLContext.read.parquet()或者SQLContext.read.load()方法，那么Spark SQL就会自动根据目录结构，推断出分区信息，是gender和country。即使数据文件中只包含了两列值，name和age，但是Spark SQL返回的DataFrame，调用printSchema()方法时，会打印出四个列的值：name，age，country，gender。这就是自动分区推断的功能。
- 此外，分区列的数据类型，也是自动被推断出来的。目前，Spark SQL仅支持自动推断出数字类型和字符串类型。有时，用户也许不希望Spark SQL自动推断分区列的数据类型。此时只要设置一个配置即可， spark.sql.sources.partitionColumnTypeInference.enabled，默认为true，即自动推断分区列的类型，设置为false，即不会自动推断类型。禁止自动推断分区列的类型时，所有分区列的类型，就统一默认都是String。

案例：自动推断用户数据的性别和国家

[Java版本](src/java/ParquetPartitionDiscovery.java)

## 合并元数据

- 如同ProtocolBuffer，Avro，Thrift一样，Parquet也是支持元数据合并的。用户可以在一开始就定义一个简单的元数据，然后随着业务需要，逐渐往元数据中添加更多的列。在这种情况下，用户可能会创建多个Parquet文件，有着多个不同的但是却互相兼容的元数据。Parquet数据源支持自动推断出这种情况，并且进行多个Parquet文件的元数据的合并。
- 因为元数据合并是一种相对耗时的操作，而且在大多数情况下不是一种必要的特性，从Spark 1.5.0版本开始，默认是关闭Parquet文件的自动合并元数据的特性的。可以通过以下两种方式开启Parquet数据源的自动合并元数据的特性：

1. 读取Parquet文件时，将数据源的选项，mergeSchema，设置为true
2. 使用`SQLContext.setConf()`方法，将`spark.sql.parquet.mergeSchema`参数设置为true

案例：合并学生的基本信息，和成绩信息的元数据

[Scala版本](src/scala/ParquetMergeSchema.scala)