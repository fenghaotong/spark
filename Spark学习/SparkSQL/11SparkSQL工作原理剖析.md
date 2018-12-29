# SparkSQL工作原理剖析

## 工作原理

- SqlParse
- Analyser
- Optimizer
- SparkPlan

## 性能优化



- 设置Shuffle过程中的并行度：`spark.sql.shuffle.partitions（SQLContext.setConf()）`
- 在Hive数据仓库建设过程中，合理设置数据类型，比如能设置为INT的，就不要设置为BIGINT。减少数据类型导致的不必要的内存开销。
- 编写SQL时，尽量给出明确的列名，比如`select name from students`。不要写`select *`的方式。
- **并行处理查询结果**：对于Spark SQL查询的结果，如果数据量比较大，比如超过1000条，那么就不要一次性collect()到Driver再处理。使用foreach()算子，并行处理查询结果。
- **缓存表**：对于一条SQL语句中可能多次使用到的表，可以对其进行缓存，使用`SQLContext.cacheTable(tableName)`，或者`DataFrame.cache()`即可。Spark SQL会用内存列存储的格式进行表的缓存。然后Spark SQL就可以仅仅扫描需要使用的列，并且自动优化压缩，来最小化内存使用和GC开销。`SQLContext.uncacheTable(tableName)`可以将表从缓存中移除。用`SQLContext.setConf()`，设置`spark.sql.inMemoryColumnarStorage.batchSize`参数（默认10000），可以配置列存储的单位。
- **广播join表**：`spark.sql.autoBroadcastJoinThreshold`，默认`10485760 (10 MB)`。在内存够用的情况下，可以增加其大小，概参数设置了一个表在join的时候，最大在多大以内，可以被广播出去优化性能。
- 钨丝计划：`spark.sql.tungsten.enabled`，默认是true，自动管理内存。

> 最有效的，其实就是并行处理查询、缓存表和广播join表，也是非常不错的！

![](img\SparkSQL工作原理剖析.png)