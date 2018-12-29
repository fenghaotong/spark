# UDF自定义函数和UDAF自定义聚合函数

## UDF

用户自定义函数。

[Scala版本实例](src/scala/UDF.scala)



## UDAF

- UDAF：User Defined Aggregate Function。用户自定义聚合函数。是Spark 1.5.x引入的最新特性。
- UDF，其实更多的是针对单行输入，返回一个输出
- UDAF，则可以针对多行输入，进行聚合计算，返回一个输出，功能更加强大

[Scala版本实例](src/scala/StringCount.scala)

[Scala版本实例](src/scala/UDAF.scala)



