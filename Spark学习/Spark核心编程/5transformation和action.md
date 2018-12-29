# transformation和action

## transformation和action介绍

- Spark支持两种`RDD`操作：`transformation和action`。transformation操作会针对已有的`RDD`创建一个新的`RDD`；而action则主要是对`RDD`进行最后的操作，比如遍历、reduce、保存到文件等，并可以返回结果给Driver程序。

  > 例如，map就是一种transformation操作，它用于将已有`RDD`的每个元素传入一个自定义的函数，并获取一个新的元素，然后将所有的新元素组成一个新的`RDD`。而reduce就是一种action操作，它用于对`RDD`中的所有元素进行聚合操作，并获取一个最终的结果，然后返回给Driver程序。

- transformation的特点就是**lazy特性**。**lazy特性**指的是，如果一个spark应用中只定义了transformation操作，那么即使你执行该应用，这些操作也不会执行。也就是说，transformation是不会触发spark程序的执行的，它们只是记录了对`RDD`所做的操作，但是不会自发的执行。只有当transformation之后，接着执行了一个action操作，那么所有的transformation才会执行。Spark通过这种lazy特性，来进行底层的spark应用执行的优化，避免产生过多中间结果。
- action操作执行，会触发一个spark job的运行，从而触发这个action之前所有的transformation的执行。这是action的特性。

**transformation和action原理剖析图：**

![transformation和action原理剖析](img\transformation和action原理剖析.png)

## 案例：统计文件字数

这里通过一个之前学习过的案例，统计文件字数，来讲解transformation和action。

```scala
// 这里通过textFile()方法，针对外部文件创建了一个RDD，lines，但是实际上，程序执行到这里为止，spark.txt文件的数据是不会加载到内存中的。lines，只是代表了一个指向spark.txt文件的引用。

val lines = sc.textFile("spark.txt")

// 这里对lines RDD进行了map算子，获取了一个转换后的lineLengths RDD。但是这里连数据都没有，当然也不会做任何操作。lineLengths RDD也只是一个概念上的东西而已。

val lineLengths = lines.map(line => line.length)

// 之列，执行了一个action操作，reduce。此时就会触发之前所有transformation操作的执行，Spark会将操作拆分成多个task到多个机器上并行执行，每个task会在本地执行map操作，并且进行本地的reduce聚合。最后会进行一个全局的reduce聚合，然后将结果返回给Driver程序。

val totalLength = lineLengths.reduce(_ + _)
```



## 案例：统计文件每行出现的次数

- Spark有些特殊的算子，也就是特殊的transformation操作。比如groupByKey、sortByKey、reduceByKey等，其实只是针对特殊的RDD的。即包含key-value对的RDD。而这种RDD中的元素，实际上是scala中的一种类型，即Tuple2，也就是包含两个值的Tuple。
- 在scala中，需要手动导入Spark的相关隐式转换，import org.apache.spark.SparkContext._。然后，对应包含Tuple2的RDD，会自动隐式转换为PairRDDFunction，并提供reduceByKey等方法。

```scala
val lines = sc.textFile("hello.txt")
val linePairs = lines.map(line => (line, 1))
val lineCounts = linePairs.reduceByKey(_ + _)
lineCounts.foreach(lineCount => println(lineCount._1 + " appears " + llineCount._2 + " times."))
```

[实例java版](../src/java/LineCount.java)

[实例scala版](../src/scala/LineCount.scala)

## 常用transformation介绍

| 操作        | 介绍                                                         |
| ----------- | ------------------------------------------------------------ |
| map         | 将RDD中的每个元素传入自定义函数，获取一个新的元素，然后用新的元素组成新的RDD |
| filter      | 对RDD中每个元素进行判断，如果返回true则保留，返回false则剔除。 |
| flatMap     | 与map类似，但是对每个元素都可以返回一个或多个新元素。        |
| gropuByKey  | 根据key进行分组，每个key对应一个`Iterable<value>  `          |
| reduceByKey | 对每个key对应的value进行reduce操作。                         |
| sortByKey   | 对每个key对应的value进行排序操作。                           |
| join        | 对两个包含`<key,value>`对的RDD进行join操作，每个key   join上的pair，都会传入自定义函数进行处理。 |
| cogroup     | 同join，但是是每个key对应的`Iterable<value>`都会传入自定义函数进行处理。 |

[实例java版](../src/java/TransformationOperation.java)

[实例scala版](../src/scala/TransformationOperation.scala)

## 常用action介绍

| 操作           | 介绍                                                         |
| -------------- | ------------------------------------------------------------ |
| reduce         | 将RDD中的所有元素进行聚合操作。第一个和第二个元素聚合，值与第三个元素聚合，值与第四个元素聚合，以此类推。 |
| collect        | 将RDD中所有元素获取到本地客户端。                            |
| count          | 获取RDD元素总数。                                            |
| take(n)        | 获取RDD中前n个元素。                                         |
| saveAsTextFile | 将RDD元素保存到文件中，对每个元素调用toString方法            |
| countByKey     | 对每个key对应的值进行count计数。                             |
| foreach        | 遍历RDD中的每个元素。                                        |

[实例java版](../src/java/ActionOperation.java)

[实例scala版](../src/scala/ActionOperation.scala)