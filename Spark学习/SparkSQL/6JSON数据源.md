# JSON数据源

- Spark SQL可以自动推断JSON文件的元数据，并且加载其数据，创建一个DataFrame。可以使用SQLContext.read.json()方法，针对一个元素类型为String的RDD，或者是一个JSON文件。

- 但是要注意的是，这里使用的JSON文件与传统意义上的JSON文件是不一样的。每行都必须，也只能包含一个，单独的，自包含的，有效的JSON对象。不能让一个JSON对象分散在多行。否则会报错。



  综合性复杂案例：查询成绩为80分以上的学生的基本信息与成绩信息

  [Java](src/java/JSONDataSource.java)

  [Scala](src/scala/JSONDataSource.scala)

