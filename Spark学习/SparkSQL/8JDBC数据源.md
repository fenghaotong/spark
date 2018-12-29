# JDBC数据源

- Spark SQL支持使用JDBC从关系型数据库（比如MySQL）中读取数据。读取的数据，依然由DataFrame表示，可以很方便地使用Spark Core提供的各种算子进行处理。
- 这里有一个经验之谈，实际上用Spark SQL处理JDBC中的数据是非常有用的。比如说，你的MySQL业务数据库中，有大量的数据，比如1000万，然后，你现在需要编写一个程序，对线上的脏数据某种复杂业务逻辑的处理，甚至复杂到可能涉及到要用Spark SQL反复查询Hive中的数据，来进行关联处理。
- 那么此时，用Spark SQL来通过JDBC数据源，加载MySQL中的数据，然后通过各种算子进行处理，是最好的选择。因为Spark是分布式的计算框架，对于1000万数据，肯定是分布式处理的。而如果你自己手工编写一个Java程序，那么不好意思，你只能分批次处理了，先处理2万条，再处理2万条，可能运行完你的Java程序，已经是几天以后的事情了。

Java版本

```java
Map<String, String> options = new HashMap<String, String>();

options.put("url", "jdbc:mysql://spark1:3306/testdb");
options.put("dbtable", "students");

DataFrame jdbcDF = sqlContext.read().format("jdbc"). options(options).load();
```

Scala版本

```scala
val jdbcDF = sqlContext.read.format("jdbc").options( 
  Map("url" -> "jdbc:mysql://spark1:3306/testdb",
  "dbtable" -> "students")).load()
```

案例：查询分数大于80分的学生信息

```sql
grant all on testdb.* to ''@'spark1' with grant option;

flush privileges;
```

[Java版本](src/java/JDBCDataSource.java)

[Scala版本](src/scala/JDBCDataSource.scala)