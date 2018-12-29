# 使用反射方式将RDD转换为DataFrame

## RDD转换为DataFrame

- 为什么要将RDD转换为DataFrame？因为这样的话，我们就可以直接针对HDFS等任何可以构建为RDD的数据，使用Spark SQL进行SQL查询了。这个功能是无比强大的。想象一下，针对HDFS中的数据，直接就可以使用SQL进行查询。

- Spark SQL支持两种方式来将RDD转换为DataFrame。

  第一种方式，是使用反射来推断包含了特定数据类型的RDD的元数据。这种基于反射的方式，代码比较简洁，当你已经知道你的RDD的元数据时，是一种非常不错的方式。

  第二种方式，是通过编程接口来创建DataFrame，你可以在程序运行时动态构建一份元数据，然后将其应用到已经存在的RDD上。这种方式的代码比较冗长，但是如果在编写程序时，还不知道RDD的元数据，只有在程序运行时，才能动态得知其元数据，那么只能通过这种动态构建元数据的方式。

## 使用反射方式推断元数据

- Java版本：Spark SQL是支持将包含了JavaBean的RDD转换为DataFrame的。JavaBean的信息，就定义了元数据。Spark SQL现在是不支持将包含了嵌套JavaBean或者List等复杂数据的JavaBean，作为元数据的。只支持一个包含简单数据类型的field的JavaBean。

[Java版](src/java/RDD2DataFrameReflection.java)

- Scala版本：而Scala由于其具有隐式转换的特性，所以Spark SQL的Scala接口，是支持自动将包含了case class的RDD转换为DataFrame的。case class就定义了元数据。Spark SQL会通过反射读取传递给case class的参数的名称，然后将其作为列名。与Java不同的是，Spark SQL是支持将包含了嵌套数据结构的case class作为元数据的，比如包含了Array等。

[Scala版](src/scala/RDD2DataFrameReflection.scala)

## 使用编程方式指定元数据

- Java版本：当JavaBean无法预先定义和知道的时候，比如要动态从一个文件中读取数据结构，那么就只能用编程方式动态指定元数据了。首先要从原始RDD创建一个元素为Row的RDD；其次要创建一个StructType，来代表Row；最后将动态定义的元数据应用到`RDD<Row>`上。

[Java版](src/java/RDD2DataFrameProgrammatically.java)

- Scala版本：Scala的实现方式，与Java是基本一样的。

[Scala版](src/scala/RDD2DataFrameProgrammatically.scala)