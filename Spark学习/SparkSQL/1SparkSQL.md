# SparkSQL

- Spark 1.0版本开始，推出了Spark SQL。其实最早使用的，都是Hadoop自己的Hive查询引擎；但是后来Spark提供了Shark；再后来Shark被淘汰，推出了Spark SQL。Shark的性能比Hive就要高出一个数量级，而Spark SQL的性能又比Shark高出一个数量级。

- 最早来说，Hive的诞生，主要是因为要让那些不熟悉Java，无法深入进行MapReduce编程的数据分析师，能够使用他们熟悉的关系型数据库的SQL模型，来操作HDFS上的数据。因此推出了Hive。Hive底层基于MapReduce实现SQL功能，能够让数据分析人员，以及数据开发人员，方便的使用Hive进行数据仓库的建模和建设，然后使用SQL模型针对数据仓库中的数据进行统计和分析。但是Hive有个致命的缺陷，就是它的底层基于MapReduce，而MapReduce的shuffle又是基于磁盘的，因此导致Hive的性能异常低下。进场出现复杂的SQL ETL，要运行数个小时，甚至数十个小时的情况。

- 后来，Spark推出了Shark，Shark与Hive实际上还是紧密关联的，Shark底层很多东西还是依赖于Hive，但是修改了内存管理、物理计划、执行三个模块，底层使用Spark的基于内存的计算模型，从而让性能比Hive提升了数倍到上百倍。

- 然而，Shark还是它的问题所在，Shark底层依赖了Hive的语法解析器、查询优化器等组件，因此对于其性能的提升还是造成了制约。所以后来Spark团队决定，完全抛弃Shark，推出了全新的Spark SQL项目。Spark SQL就不只是针对Hive中的数据了，而且可以支持其他很多数据源的查询。

- Spark SQL的特点

  1. 支持多种数据源：Hive、RDD、Parquet、JSON、JDBC等。
  2. 多种性能优化技术：in-memory columnar storage、byte-code generation、cost model动态评估等。
  3. 组件扩展性：对于SQL的语法解析器、分析器以及优化器，用户都可以自己重新开发，并且动态扩展。



  > 在2014年6月1日的时候，Spark宣布了不再开发Shark，全面转向Spark SQL的开发。
  >
  >
  >
  > Spark SQL的性能比Shark来说，又有了数倍的提升。

- Spark SQL的性能优化技术简介

  1. 内存列存储（in-memory columnar storage）

     内存列存储意味着，Spark SQL的数据，不是使用Java对象的方式来进行存储，而是使用面向列的内存存储的方式来进行存储。也就是说，每一列，作为一个数据存储的单位。从而大大优化了内存使用的效率。采用了内存列存储之后，减少了对内存的消耗，也就避免了gc大量数据的性能开销。

  2. 字节码生成技术（byte-code generation）

     Spark SQL在其catalyst模块的expressions中增加了codegen模块，对于SQL语句中的计算表达式，比如select num + num from t这种的sql，就可以使用动态字节码生成技术来优化其性能。

  3. Scala代码编写的优化

     对于Scala代码编写中，可能会造成较大性能开销的地方，自己重写，使用更加复杂的方式，来获取更好的性能。比如Option样例类、for循环、map/filter/foreach等高阶函数，以及不可变对象，都改成了用null、while循环等来实现，并且重用可变的对象。