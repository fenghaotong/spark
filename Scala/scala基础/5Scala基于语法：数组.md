## Scala基于语法：数组

### Array

创建空数组

```scala
val a = new Array[Int](10)
val a = new Array[String](10)

val a = Array("hello", "world")
a(0) = "hi"
```

### ArrayBuffer

在Scala中，如果需要类似于Java中的ArrayList这种长度可变的集合类，则可以使用ArrayBuffer。

```scala
scala> import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArrayBuffer

scala> val b = ArrayBuffer[Int]()
b: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer()

scala> b += 1
res24: b.type = ArrayBuffer(1)

scala> b +=(2,3,4,5)
res26: b.type = ArrayBuffer(1, 2, 3, 4, 5)

scala> b ++= Array(6,7,8,9,10)
res28: b.type = ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> b.trimEnd(5)  //  使用trimEnd()函数，可以从尾部截断指定个数的元素

scala> b
res30: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(1, 2, 3, 4, 5)
```

也可以使用insert()函数可以在指定位置插入元素

```scala
scala> b.insert(5, 6)  // 第一个参数时位置
scala> b.insert(6, 7, 8, 9, 10)

scala> 
scala> 
```

使用remove()函数可以移除指定位置的元素

```scala
scala> b.remove(1)
scala> b.remove(1, 3)
```

Array与ArrayBuffer可以互相进行转换

```scala
scala> b.toArray
scala> a.toBuffer
```

### 便利Array和ArrayBuffer

```scala
scala> for(i <- 0 until b.length) println(b(i))
1
3
3
6
7
8
9
10
scala> for(i <- 0 until (b.length,2)) println(b(i))  // 间隔为2
1
3
7
9

scala> for(i <- (0 until b.length).reverse) println(b(i)) // 倒序
10
9
8
7
6
3
3
1

scala> for(e <- b) println(e) // 增强for循环
1
3
3
6
7
8
9
10

scala>
```

### 数据常见操作

- 求和
- 最大值
- 排序
- 获取所有元素内容

###  使用yield和函数式编程转换数据

对Array进行转换，获取的还是Array

```scala
scala> val a = Array(1,2,3,4,5)
a: Array[Int] = Array(1, 2, 3, 4, 5)

scala> val a2 = for(ele <- a) yield ele * ele
a2: Array[Int] = Array(1, 4, 9, 16, 25)
```

对ArrayBuffer进行转换，获取的还是ArrayBuffer

```scala
scala> val b = ArrayBuffer[Int]()
b: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer()

scala> b += (1,2,3,4,5)
res55: b.type = ArrayBuffer(1, 2, 3, 4, 5)

scala> val b2 = for(ele <- b) yield ele * ele
b2: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(1, 4, 9, 16, 25)
```

结合if守卫，仅转换需要的元素

```scala
scala> val a3 = for(ele <- a if ele % 2 == 0) yield ele * ele
a3: Array[Int] = Array(4, 16)

```

使用函数式编程转换数组

```scala
scala> a.filter(_ % 2 == 0).map(_ * 2)
res57: Array[Int] = Array(4, 8)

```

