# Scala编程：函数式编程

## Scala函数式编程语法

### 将函数赋值给变量

- `Scala`中的函数是一等公民，可以独立定义，独立存在，而且可以直接将函数作为值赋值给变量
- `Scala`的语法规定，将函数赋值给变量时，必须在函数后面加上空格和下划线

```scala
def sayHello(name: String) { println("Hello, " + name) }
val sayHelloFunc = sayHello _
sayHelloFunc("leo")

```



### 匿名函数

- `Scala`中，函数也可以不需要命名，此时函数被称为匿名函数。
- 可以直接定义函数之后，将函数赋值给某个变量；也可以将直接定义的匿名函数传入其他函数之中
- `Scala`定义匿名函数的语法规则就是，`(参数名:参数类型) => 函数体`
- 这种匿名函数的语法必须深刻理解和掌握，在spark的中有大量这样的语法，如果没有掌握，是看不懂`spark`源码的

```scala
val sayHelloFunc = (name: String) => println("Hello, " + name)

```



### 高阶函数

- `Scala`中，由于函数是一等公民，因此可以直接将某个函数传入其他函数，作为参数。这个功能是极其强大的，也是`Java`这种面向对象的编程语言所不具备的。
- 接收其他函数作为参数的函数，也被称作高阶函数（`higher-order function`）

```scala
val sayHelloFunc = (name: String) => println("Hello, " + name)
def greeting(func: (String) => Unit, name: String) { func(name) }
greeting(sayHelloFunc, "leo")

```

高阶函数的另外一个功能是将函数作为返回值

```scala
def getGreetingFunc(msg: String) = (name: String) => println(msg + ", " + name)
val greetingFunc = getGreetingFunc("hello")
greetingFunc("leo")

```



### 高阶函数的类型推断

- 高阶函数可以自动推断出参数类型，而不需要写明类型；而且对于只有一个参数的函数，还可以省去其小括号；如果仅有的一个参数在右侧的函数体内只使用一次，则还可以将接收参数省略，并且将参数用_来替代
- 诸如`3 * _`的这种语法，必须掌握！！`spark`源码中大量使用了这种语法！

```scala
def greeting(func: (String) => Unit, name: String) { func(name) }
greeting((name: String) => println("Hello, " + name), "leo")
greeting((name) => println("Hello, " + name), "leo")
greeting(name => println("Hello, " + name), "leo")

def triple(func: (Int) => Int) = { func(3) }
triple(3 * _)

```



### Scala的常用高阶函数

- `map`: 对传入的每个元素都进行映射，返回一个处理后的元素

```scala
Array(1, 2, 3, 4, 5).map(2 * _)
```

- `foreach`:对传入的每个元素都进行处理，但是没有返回值

```scala
(1 to 9).map("*" * _).foreach(println _)
```

- `filter`:对传入的每个元素都进行条件判断，如果对元素返回true，则保留该元素，否则过滤掉该元素

```scala
(1 to 20).filter(_ % 2 == 0)
```

- `reduceLeft:`从左侧元素开始，进行reduce操作，即先对元素1和元素2进行处理，然后将结果与元素3处理，再将结果与元素4处理，依次类推，即为r`educe`；`reduce`操作必须掌握！spark编程的重点！！！
- 下面这个操作就相当于`1 * 2 * 3 * 4 * 5 * 6 * 7  * 8 * 9`

```scala
(1 to 9).reduceLeft( _ * _)
```

- `sortWith`: 对元素进行两两相比，进行排序

```scala
Array(3, 2, 5, 4, 10, 1).sortWith(_ < _)
```

### 闭包

- 闭包最简洁的解释：函数在变量不处于其有效作用域时，还能够对变量进行访问，即为闭包

```scala
def getGreetingFunc(msg: String) = (name: String) => println(msg + ", " + name)
val greetingFuncHello = getGreetingFunc("hello")
val greetingFuncHi = getGreetingFunc("hi")
```

- 两次调用`getGreetingFunc`函数，传入不同的`msg`，并创建不同的函数返回
- 然而，`msg`只是一个局部变量，却在`getGreetingFunc`执行完之后，还可以继续存在创建的函数之中；`greetingFuncHello("leo")`，调用时，值为`"hello"`的`msg`被保留在了函数体内部，可以反复的使用
- 这种变量超出了其作用域，还可以使用的情况，即为闭包
- `Scala`通过为每个函数创建对象来实现闭包，实际上对于`getGreetingFunc`函数创建的函数，`msg`是作为函数对象的变量存在的，因此每个函数才可以拥有不同的`msg`
- `Scala`编译器会确保上述闭包机制

### SAM转换

- 在`Java`中，不支持直接将函数传入一个方法作为参数，通常来说，唯一的办法就是定义一个实现了某个接口的类的实例对象，该对象只有一个方法；而这些接口都只有单个的抽象方法，也就是`single abstract method`，简称为`SAM`
- 由于`Scala`是可以调用`Java`的代码的，因此当我们调用`Java`的某个方法时，可能就不得不创建`SAM`传递给方法，非常麻烦；但是`Scala`又是支持直接传递函数的。此时就可以使用`Scala`提供的，在调用`Java`方法时，使用的功能，`SAM`转换，即将`SAM`转换为`Scala`函数
- 要使用`SAM`转换，需要使用`Scala`提供的特性，隐式转换

```scala
import javax.swing._
import java.awt.event._

val button = new JButton("Click")
button.addActionListener(new ActionListener {
  override def actionPerformed(event: ActionEvent) {
    println("Click Me!!!")
  }
})

implicit def getActionListener(actionProcessFunc: (ActionEvent) => Unit) = new ActionListener {
  override def actionPerformed(event: ActionEvent) {
    actionProcessFunc(event)
  }
}
button.addActionListener((event: ActionEvent) => println("Click Me!!!"))

```



### Currying函数

- `Curring`函数，指的是，将原来接收两个参数的一个函数，转换为两个函数，第一个函数接收原先的第一个参数，然后返回接收原先第二个参数的第二个函数。
- 在函数调用的过程中，就变为了两个函数连续调用的形式
- 在`Spark`的源码中，也有体现，所以对`()()`这种形式的`Curring`函数，必须掌握！

```scala
def sum(a: Int, b: Int) = a + b
sum(1, 1)

def sum2(a: Int) = (b: Int) => a + b
sum2(1)(1)

def sum3(a: Int)(b: Int) = a + b

```



### return

- `Scala`中，不需要使用`return`来返回函数的值，函数最后一行语句的值，就是函数的返回值。在`Scala`中，`return`用于在匿名函数中返回值给包含匿名函数的带名函数，并作为带名函数的返回值.
- 使用`return`的匿名函数，是必须给出返回类型的，否则无法通过编译

```scala
def greeting(name: String) = {
  def sayHello(name: String):String = {
    return "Hello, " + name
  }
  sayHello(name)
}

```



## Scala内置函数式编程

### Scala的集合体系结构

- `Scala`中的集合体系主要包括：`Iterable、Seq、Set、Map`。其中`Iterable`是所有集合`trait`的根`trai`。这个结构与`Java`的集合体系非常相似。
- `Scala`中的集合是分成可变和不可变两类集合的，其中可变集合就是说，集合的元素可以动态修改，而不可变集合的元素在初始化之后，就无法修改了。分别对应`scala.collection.mutable`和`scala.collection.immutable`两个包。
- `Seq`下包含了`Range、ArrayBuffer、List`等子`trait`。其中Range就代表了一个序列，通常可以使用`“1 to 10”`这种语法来产生一个`Range`。 `ArrayBuffe`r就类似于`Java`中的`ArrayList`。

### List

- `List`代表一个不可变的列表
- `List`的创建，`val list = List(1, 2, 3, 4)`
- `List`有`head`和`tail`，`head`代表`List`的第一个元素，`tail`代表第一个元素之后的所有元素，`list.head`，`list.tail`
- `List`有特殊的`::`操作符，可以用于将`head`和`tail`合并成一个`List`，`0 :: list`
- `::`这种操作符要清楚，在`spark`源码中都是有体现的，一定要能够看懂！
- 如果一个`List`只有一个元素，那么它的`head`就是这个元素，它的`tail`是`Nil`
- 案例：用递归函数来给`List`中每个元素都加上指定前缀，并打印加上前缀的元素

```scala
def decorator(l: List[Int], prefix: String) {
  if (l != Nil) { 
    println(prefix + l.head)
    decorator(l.tail, prefix)
  }
}

 val list = List(1, 2, 3, 4)
 decorator(list, "count:")
```



### LinkedList

- `LinkedList`代表一个可变的列表，使用`elem`可以引用其头部，使用`next`可以引用其尾部

```scala
val l = scala.collection.mutable.LinkedList(1, 2, 3, 4, 5); l.elem; l.next
```

- 案例：使用`while`循环将`LinkedList`中的每个元素都乘以`2`

```scala
val list = scala.collection.mutable.LinkedList(1, 2, 3, 4, 5)
var currentList = list
while (currentList != Nil) {
  currentList.elem = currentList.elem * 2
  currentList = currentList.next
}

```

案例：使用`while`循环将`LinkedList`中，从第一个元素开始，每隔一个元素，乘以2

```scala
val list = scala.collection.mutable.LinkedList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
var currentList = list
var first = true
while (currentList != Nil && currentList.next != Nil) {
  if (first) { currentList.elem = currentList.elem * 2; first = false }
  currentList  = currentList.next.next
  if (currentList != Nil) currentList.elem = currentList.elem * 2
}
```



### Set

- `Set`代表一个没有重复元素的集合
- 将重复元素加入`Set`是没有用的，比如

```scala
val s = Set(1, 2, 3); s + 1; s + 4
```

- 而且`Set`是不保证插入顺序的，也就是说，`Set`中的元素是乱序的

```scala
val s = new scala.collection.mutable.HashSet[Int](); s += 1; s += 2; s += 5
```

- `LinkedHashSet`会用一个链表维护插入顺序，`val s = new `

```scala
val s = new scala.collection.mutable.LinkedHashSet[Int](); s += 1; s += 2; s += 5
```

- `SrotedSet`会自动根据key来进行排序.

```scala
val s = scala.collection.mutable.SortedSet("orange", "apple", "banana")
```



### 集合的函数式编程

- 集合的函数式编程非常非常非常之重要！！！
- 必须完全掌握和理解`Scala`的高阶函数是什么意思，`Scala`的集合类的`map、flatMap、reduce、reduceLeft、foreach`等这些函数，就是高阶函数，因为可以接收其他函数作为参数
- 高阶函数的使用，也是`Scala`与`Java`最大的一点不同！！！因为`Java`里面是没有函数式编程的，也肯定没有高阶函数，也肯定无法直接将函数传入一个方法，或者让一个方法返回一个函数
- 对`Scala`高阶函数的理解、掌握和使用，可以大大增强你的技术，而且也是`Scala`最有诱惑力、最有优势的一个功能！！！
- 此外，在`Spark`源码中，有大量的函数式编程，以及基于集合的高阶函数的使用！！！所以必须掌握，才能看懂`spark`源码

`map`案例实战：为`List`中每个元素都添加一个前缀

```scala
List("Leo", "Jen", "Peter", "Jack").map("name is " + _)
```

`faltMap`案例实战：将List中的多行句子拆分成单词

```scala
List("Hello World", "You Me").flatMap(_.split(" "))
```

`foreach`案例实战：打印List中的每个单词

```scala
List("I", "have", "a", "beautiful", "house").foreach(println(_))
```

`zip`案例实战：对学生姓名和学生成绩进行关联

```scala
List("Leo", "Jen", "Peter", "Jack").zip(List(100, 90, 75, 83))
```

### 函数式编程综合案例：统计多个文本内的单词总数

- 使用`scala`的`io`包将文本文件内的数据读取出来

```scala
val lines01 = scala.io.Source.fromFile("C://Users//htfeng//Desktop//test01.txt").mkString
val lines02 = scala.io.Source.fromFile("C://Users//htfeng//Desktop//test02.txt").mkString

```

- 使用`List`的伴生对象，将多个文件内的内容创建为一个`List`

```scala
val lines = List(lines01, lines02)
```

- 下面这一行才是我们的案例的核心和重点，因为有多个高阶函数的链式调用，以及大量下划线的使用，如果没有透彻掌握之前的课讲解的`Scala`函数式编程，那么下面这一行代码，完全可能会看不懂！！！
- 但是下面这行代码其实就是`Scala`编程的精髓所在，就是函数式编程，也是`Scala`相较于`Java`等编程语言最大的功能优势所在
- 而且，`spark`的源码中大量使用了这种复杂的链式调用的函数式编程
- 而且，`spark`本身提供的开发人员使用的编程`api`的风格，完全沿用了`Scala`的函数式编程，比如`Spark`自身的`api`中就提供了`map、flatMap、reduce、foreach`，以及更高级的`reduceByKey、groupByKey`等高阶函数
- 如果要使用`Scala`进行`spark`工程的开发，那么就必须掌握这种复杂的高阶函数的链式调用！！！

```scala
lines.flatMap(_.split(" ")).map((_, 1)).map(_._2).reduceLeft(_ + _)
```



