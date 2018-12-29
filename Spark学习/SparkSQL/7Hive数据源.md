# Hive数据源

- Spark SQL支持对Hive中存储的数据进行读写。操作Hive中的数据时，必须创建HiveContext，而不是SQLContext。HiveContext继承自SQLContext，但是增加了在Hive元数据库中查找表，以及用HiveQL语法编写SQL的功能。除了sql()方法，HiveContext还提供了hql()方法，从而用Hive语法来编译sql。
- 使用HiveContext，可以执行Hive的大部分功能，包括创建表、往表里导入数据以及用SQL语句查询表中的数据。查询出来的数据是一个Row数组。
- 将hive-site.xml拷贝到spark/conf目录下，将mysql
  connector拷贝到spark/lib目录下

```scala
HiveContext sqlContext = new HiveContext(sc);
sqlContext.sql("CREATE TABLE IF NOT EXISTS students (name STRING, age INT)");
sqlContext.sql("LOAD DATA LOCAL INPATH '/usr/local/spark-study/resources/students.txt' INTO TABLE students");
Row[] teenagers = sqlContext.sql("SELECT name, age FROM students WHERE age<=18").collect();

```



- Spark SQL还允许将数据保存到Hive表中。调用DataFrame的saveAsTable命令，即可将DataFrame中的数据保存到Hive表中。与registerTempTable不同，saveAsTable是会将DataFrame中的数据物化到Hive表中的，而且还会在Hive元数据库中创建表的元数据。
- 默认情况下，saveAsTable会创建一张Hive Managed Table，也就是说，数据的位置都是由元数据库中的信息控制的。当Managed Table被删除时，表中的数据也会一并被物理删除。
- registerTempTable只是注册一个临时的表，只要Spark Application重启或者停止了，那么表就没了。而saveAsTable创建的是物化的表，无论Spark Application重启或者停止，表都会一直存在。
- 调用HiveContext.table()方法，还可以直接针对Hive中的表，创建一个DataFrame。
- 案例：查询分数大于80分的学生的完整信息

[Java版本](src/java/HiveDataSource.java)

[Scala版本](src/scala/HiveDataSource.scala)