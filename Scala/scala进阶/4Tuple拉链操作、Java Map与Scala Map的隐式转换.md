# Tuple拉链操作、Java Map与Scala Map的隐式转换 

## Tuple拉链操作

- Tuple拉链操作指的就是zip操作

- zip操作，是Array类的方法，用于将两个Array，合并为一个Array

- 比如Array(v1)和Array(v2)，使用zip操作合并后的格式为Array((v1,v2))

- 合并后的Array的元素类型为Tuple

  ```scala
  val students = Array("Leo", "Jack", "Jen")
  val scores = Array(80, 100, 90)
  val studentScores = students.zip(scores)
  
  for ((student, score) <- studentScores)
    println(student + " " + score)
  
  ```

- 如果Array的元素类型是个Tuple，调用Array的toMap方法，可以将Array转换为Map 

  ```scala
  studentScores.toMap
  ```

## Java Map与Scala Map的隐式转换

```scala
import scala.collection.JavaConversions.mapAsScalaMap

val javaScores = new java.util.HashMap[String, Int]()
javaScores.put("Alice", 10)
javaScores.put("Bob", 3)
javaScores.put("Cindy", 8)

val scalaScores: scala.collection.mutable.Map[String, Int] = javaScores

import scala.collection.JavaConversions.mapAsJavaMap
import java.awt.font.TextAttribute._
val scalaAttrMap = Map(FAMILY -> "Serif", SIZE -> 12)
val font = new java.awt.Font(scalaAttrMap)

```



