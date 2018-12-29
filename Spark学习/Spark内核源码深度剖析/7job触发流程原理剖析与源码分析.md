# job触发流程原理剖析

```scala
val lines = sc.textFile()
val words = lines.flatMap(line => line.split(" "))
val pairs = words.map(word => (word, 1))

```

- 其实RDD里是没有reduceByKey的，因此对RDD调用reduceByKey()方法的时候，会触发scala的隐式转换；此时就会在作用域内，寻找隐式转换，会在RDD中找到rddToPairRDDFunctions()隐式转换，然后将RDD转换为PairRDDFunctions。
-  接着会调用PairRDDFunctions中的reduceByKey()方法

```scala
val counts = pairs.reduceByKey(_ + _)

counts.foreach(count => println(count._1 + ": " + count._2))

```

