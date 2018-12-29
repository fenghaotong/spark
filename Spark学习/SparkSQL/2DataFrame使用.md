# DataFrame使用

## Spark SQL and DataFrame引言

- Spark SQL是Spark中的一个模块，主要用于进行结构化数据的处理。它提供的最核心的编程抽象，就是DataFrame。同时Spark SQL还可以作为分布式的SQL查询引擎。Spark SQL最重要的功能之一，就是从Hive中查询数据。
- DataFrame，可以理解为是，以列的形式组织的，分布式的数据集合。它其实和关系型数据库中的表非常类似，但是底层做了很多的优化。DataFrame可以通过很多来源进行构建，包括：结构化的数据文件，Hive中的表，外部的关系型数据库，以及RDD。

## SQLContext

- 要使用Spark SQL，首先就得创建一个创建一个SQLContext对象，或者是它的子类的对象，比如HiveContext的对象。

## HiveContext

- 除了基本的SQLContext以外，还可以使用它的子类——HiveContext。HiveContext的功能除了包含SQLContext提供的所有功能之外，还包括了额外的专门针对Hive的一些功能。这些额外功能包括：使用HiveQL语法来编写和执行SQL，使用Hive中的UDF函数，从Hive表中读取数据。
- 要使用HiveContext，就必须预先安装好Hive，SQLContext支持的数据源，HiveContext也同样支持——而不只是支持Hive。对于Spark 1.3.x以上的版本，都推荐使用HiveContext，因为其功能更加丰富和完善。
- Spark SQL还支持用spark.sql.dialect参数设置SQL的方言。使用SQLContext的setConf()即可进行设置。对于SQLContext，它只支持“sql”一种方言。对于HiveContext，它默认的方言是“hiveql”。

## 创建DataFrame

- 使用SQLContext，可以从RDD、Hive表或者其他数据源，来创建一个DataFrame。以下是一个使用JSON文件创建DataFrame的例子：

Java版本：

```java
JavaSparkContext sc = ...; 

SQLContext sqlContext = new SQLContext(sc);

DataFrame df = sqlContext.read().json("hdfs://spark1:9000/students.json");

df.show();
```

[Java版](src/java/DataFrameCreate.java)

Scala版本：

```scala
val sc: SparkContext = ...

val sqlContext = new SQLContext(sc)

val df = sqlContext.read.json("hdfs://spark1:9000/students.json")

df.show()
```

[Scala版](src/scala/DataFrameCreate.scala)

## DataFrame的常用操作

Java版本

```java
DataFrame df = sqlContext.read().json("hdfs://spark1:9000/students.json");

df.show();

df.printSchema();

df.select("name").show();

df.select(df.col("name"), df.col("age").plus(1)).show();

df.filter(df.col("age").gt(21)).show();

df.groupBy("age").count().show();
```

[Java版](src/java/DataFrameOperation.java)

Scala版本

```scala
val df = sqlContext.read.json("hdfs://spark1:9000/students.json")

df.show()

df.printSchema()

df.select("name").show()

df.select(df("name"), df("age") + 1).show()

df.filter(df("age") > 21).show()

df.groupBy("age").count().show()
```

[Scala版](src/scala/DataFrameOperation.scala)

