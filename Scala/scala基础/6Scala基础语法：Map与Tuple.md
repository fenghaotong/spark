## Scala基础语法：Map与Tuple

### 创建Map

创建一个不可变的Map

```scala
scala> val ages=Map("leo"->30, "Jen"->25)
ages: scala.collection.immutable.Map[String,Int] = Map(leo -> 30, Jen -> 25)

scala> ages("leo")
res65: Int = 30

scala> val ages = Map(("Leo", 30), ("Jen", 25), ("Jack", 23))
```

创建一个可变的Map

```scala
scala> val ages=scala.collection.mutable.Map("leo"->30, "Jen"->25)
ages: scala.collection.mutable.Map[String,Int] = Map(Jen -> 25, leo -> 30)

scala> ages("leo") = 31

scala> ages("leo")
res67: Int = 31

```

创建一个空的HashMap

```scala
scala> val ages=new scala.collection.mutable.HashMap[String, Int]
ages: scala.collection.mutable.HashMap[String,Int] = Map()

```

### 修改Map的元素

访问Map元素

```scala
scala> val leoAge = ages.getOrElse("Leo", 0)  // 如果没有Leo，返回0
leoAge: Int = 0

```

增加多个元素

```scala
scala> ages += ("Mike" -> 35, "Tom" -> 40)
res68: ages.type = Map(Jen -> 25, Mike -> 35, leo -> 30, Tom -> 40)
```

移除元素

```scala
scala> ages -= "Mike"
res69: ages.type = Map(Jen -> 25, leo -> 30, Tom -> 40)
```

更新不可变元素

```scala
scala> val ages2 = ages + ("Mike" -> 36, "Tom" -> 40)
ages2: scala.collection.mutable.Map[String,Int] = Map(Jen -> 25, Mike -> 36, leo -> 30, Tom -> 40)
```

移除不可变map的元素

```scala
scala> val ages3 = ages - "Tom"
ages3: scala.collection.mutable.Map[String,Int] = Map(Jen -> 25, leo -> 30)

```

### 遍历Map

遍历map的entrySet

```scala
scala> for ((key, value) <- ages) println(key + " " + value)
Jen 25
leo 30
Tom 40

```

遍历map的key

```scala
scala> for (key <- ages.keySet) println(key)
Jen
leo
Tom

```

遍历map的value

```scala
scala> for (value <- ages.values) println(value)
25
30
40

```

生成新map，反转key和value

```scala
scala> for ((key, value) <- ages) yield (value, key)
res73: scala.collection.mutable.Map[Int,String] = Map(40 -> Tom, 25 -> Jen, 30 -> leo)

```

### SortedMap和LinkedHashMap

SortedMap可以自动对Map的key排序

```scala
scala> val ages = scala.collection.immutable.SortedMap("leo" -> 30, "alice" -> 15, "jen" -> 25)
ages: scala.collection.immutable.SortedMap[String,Int] = Map(alice -> 15, jen -> 25, leo -> 30)

```

LinkedHashMap可以记住插入的顺序

```scala
scala> val ages = new scala.collection.mutable.LinkedHashMap[String, Int]
ages: scala.collection.mutable.LinkedHashMap[String,Int] = Map()

scala> ages("leo") = 30

scala> ages("alice") = 15

scala> ages("jen") = 25

scala> ages
res78: scala.collection.mutable.LinkedHashMap[String,Int] = Map(leo -> 30, alice -> 15, jen -> 25)

```

## Tuple

```scala
scala> val t=("leo", 30)
t: (String, Int) = (leo,30)

scala> t._1
res79: String = leo

scala> t._2
res80: Int = 30
```

### zip操作

```scala
scala> val names = Array("leo", "jack", "mike")
names: Array[String] = Array(leo, jack, mike)

scala> val ages = Array(30, 23, 34)
ages: Array[Int] = Array(30, 23, 34)

scala> val nameAges = names.zip(ages)
nameAges: Array[(String, Int)] = Array((leo,30), (jack,23), (mike,34))

scala> for ((name, age) <- nameAges) println(name + ": " + age)
leo: 30
jack: 23
mike: 34

```



