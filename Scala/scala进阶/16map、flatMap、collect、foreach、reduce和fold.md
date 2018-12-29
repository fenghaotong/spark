# map、flatMap、collect、foreach、reduce和fold

**map**

```scala
val scoreMap = Map("leo" -> 90, "jack" -> 60, "tom" -> 70)
val names = List("leo", "jack", "tom")
names.map(scoreMap(_))
```

**flatmap**

```scala
val scoreMap = Map("leo" -> List(80, 90, 60), "jack" -> List(70, 90, 50), "tom" -> List(60,70,40))
names.map(scoreMap(_))
names.flatMap(scoreMap(_))
```

**collect操作，结合偏函数使用**

```scala
"abc".collect { case 'a' => 1; case 'b' => 2; case 'c' => 3 }
```

**foreach操作，遍历**

```scala
names.foreach(println _)
```

**reduce**

```scala
List(1, 2, 3, 4).reduceLeft(_ - _)
List(1, 2, 3, 4).reduceRight(_ - _)
```

**fold**

```scala
List(1, 2, 3, 4).foldLeft(10)(_ - _)
List(1, 2, 3, 4).foldRight(10)(_ - _)
```

