# 模式匹配

- 模式匹配是`Scala`中非常有特色，非常强大的一种功能。模式匹配，其实类似于`Java`中的`swich case`语法，即对一个值进行条件判断，然后针对不同的条件，进行不同的处理。
- 但是`Scala`的模式匹配的功能比`Java`的`swich case`语法的功能要强大地多，`Java`的`swich case`语法只能对值进行匹配。但是`Scala`的模式匹配除了可以对值进行匹配之外，还可以对类型进行匹配、对`Array`和`List`的元素情况进行匹配、对`case class`进行匹配、甚至对有值或没值`（Option）`进行匹配。
- 而且对于`Spark`来说，`Scala`的模式匹配功能也是极其重要的，在`spark`源码中大量地使用了模式匹配功能。因此为了更好地编写`Scala`程序，并且更加通畅地看懂`Spark`的源码，学好模式匹配都是非常重要的。

## 模式匹配的基础语法

- `Scala`是没有`Java`中的`switch case`语法的，相对应的，`Scala`提供了更加强大的`match case`语法，即模式匹配，类替代`switch case`，`match case`也被称为模式匹配
- `Scala的match case与Java的switch case`最大的不同点在于，`Java`的`switch case`仅能匹配变量的值，比`1、2、3`等；而`Scala`的`match`
  `case`可以匹配各种情况，比如变量的类型、集合的元素、有值或无值
- `match case`的语法如下：变量` match { case 值 => 代码 }`。如果值为下划线，则代表了不满足以上所有情况下的默认情况如何处理。此外，`match case`中，只要一个`case`分支满足并处理了，就不会继续判断下一个`case`分支了。（与`Java`不同，`java的switch case`需要用`break`阻止）
- `match case`语法最基本的应用，就是对变量的值进行模式匹配

```scala
def judgeGrade(grade: String) {
  grade match {
    case "A" => println("Excellent")
    case "B" => println("Good")
    case "C" => println("Just so so")
    case _ => println("you need work harder")
  }
}
```

### 在模式匹配中使用if守卫

- `Scala`的模式匹配语法，有一个特点在于，可以在`case`后的条件判断中，不仅仅只是提供一个值，而是可以在值后面再加一个if守卫，进行双重过滤.(对特殊值进行模式匹配)

```scala
def judgeGrade(name: String, grade: String) {
  grade match {
    case "A" => println(name + ", you are excellent")
    case "B" => println(name + ", you are good")
    case "C" => println(name + ", you are just so so")
    case _ if name == "leo" => println(name + ", you are a good boy, come on")
    case _ => println("you need to work harder")
  }
}

```

### 在模式匹配中进行变量赋值

- `Scala`的模式匹配语法，有一个特点在于，可以将模式匹配的默认情况，下划线，替换为一个变量名，此时模式匹配语法就会将要匹配的值赋值给这个变量，从而可以在后面的处理语句中使用要匹配的值
- 为什么有这种语法？？思考一下。因为只要使用用case匹配到的值，是不是我们就知道这个只啦！！在这个`case`的处理语句中，是不是就直接可以使用写程序时就已知的值！
- 但是对于下划线`_`这种情况，所有不满足前面的`case`的值，都会进入_这种默认情况进行处理，此时如果我们在处理语句中需要拿到具体的值进行处理呢？那就需要使用这种在模式匹配中进行变量赋值的语法！！

```scala
def judgeGrade(name: String, grade: String) {
  grade match {
    case "A" => println(name + ", you are excellent")
    case "B" => println(name + ", you are good")
    case "C" => println(name + ", you are just so so")
    case _grade if name == "leo" => println(name + ", you are a good boy, come on, your grade is " + _grade)
    case _grade => println("you need to work harder, your grade is " + _grade)
  }
}

```



## 对类型进行模式匹配

- `Scala`的模式匹配一个强大之处就在于，可以直接匹配类型，而不是值！！！这点是`java`的`switch case`绝对做不到的。
- 理论知识：对类型如何进行匹配？其他语法与匹配值其实是一样的，但是匹配类型的话，就是要用`“case 变量: 类型 => 代码”`这种语法，而不是匹配值的`“case 值 => 代码”`这种语法。

```scala
import java.io._

def processException(e: Exception) {
  e match {
    case e1: IllegalArgumentException => println("you have illegal arguments! exception is: " + e1)
    case e2: FileNotFoundException => println("cannot find the file you need read or write!, exception is: " + e2)
    case e3: IOException => println("you got an error while you were doing IO operation! exception is: " + e3)
    case _: Exception => println("cannot know which exception you have!" )
  }
}

```



## 对Array和List的元素进行模式匹配

- 对`Array`进行模式匹配，分别可以匹配带有指定元素的数组、带有指定个数元素的数组、以某元素打头的数组
- 对`List`进行模式匹配，与`Array`类似，但是需要使用`List`特有的`::`操作符

```scala
def greeting1(arr: Array[String]) {
  arr match {
    case Array("Leo") => println("Hi, Leo!")
    case Array(girl1, girl2, girl3) => println("Hi, girls, nice to meet you. " + girl1 + " and " + girl2 + " and " + girl3)
    case Array("Leo", _*) => println("Hi, Leo, please introduce your friends to me.")
    case _ => println("hey, who are you?")
  }
}

def greeting(list: List[String]) {
  list match {
    case "Leo" :: Nil => println("Hi, Leo!")
    case girl1 :: girl2 :: girl3 :: Nil => println("Hi, girls, nice to meet you. " + girl1 + " and " + girl2 + " and " + girl3)
    case "Leo" :: tail => println("Hi, Leo, please introduce your friends to me.")
    case _ => println("hey, who are you?")
  }
}

```



## case class与模式匹配

- `Scala`中提供了一种特殊的类，用`case class`进行声明，中文也可以称作样例类。`case class`其实有点类似于`Java`中的`JavaBean`的概念。即只定义`field`，并且由`Scala`编译时自动提供`getter和setter`方法，但是没有`method`。
- `case class`的主构造函数接收的参数通常不需要使用`var`或`val`修饰，`Scala`自动就会使用`val`修饰（但是如果你自己使用`var`修饰，那么还是会按照var来）
- `Scala`自动为`case class`定义了伴生对象，也就是`object`，并且定义了`apply()`方法，该方法接收主构造函数中相同的参数，并返回`case class`对象

```scala
class Person
case class Teacher(name: String, subject: String) extends Person
case class Student(name: String, classroom: String) extends Person

def judgeIdentify(p: Person) {
  p match {
    case Teacher(name, subject) => println("Teacher, name is " + name + ", subject is " + subject)
    case Student(name, classroom) => println("Student, name is " + name + ", classroom is " + classroom)
    case _ => println("Illegal access, please go out of the school!")
  }  
}


```



## Option与模式匹配

- `Scala`有一种特殊的类型，叫做`Option`。`Option`有两种值，一种是`Some`，表示有值，一种是`None`，表示没有值。
- `Option`通常会用于模式匹配中，用于判断某个变量是有值还是没有值，这比`null`来的更加简洁明了
- `Option`的用法必须掌握，因为`Spark`源码中大量地使用了`Option`，比如`Some(a)、None`这种语法，因此必须看得懂`Option`模式匹配，才能够读懂`spark`源码。

```scala
val grades = Map("Leo" -> "A", "Jack" -> "B", "Jen" -> "C")

def getGrade(name: String) {
  val grade = grades.get(name)
  grade match {
    case Some(grade) => println("your grade is " + grade)
    case None => println("Sorry, your grade information is not in the system")
  }
}

```

