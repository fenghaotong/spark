## Scala基础语法：条件控制域循环

**if表达式的定义**

```scala
val age = 30
if(age > 18) 1 else 0
```

**if表达式的类型判断**

```scala
val age = 30
if(age > 18) "adult" else 0
```

> 自己判断返回值类型，返回公共类型`Any`

> 在`scala`的命令行可以使用`:paste`来运行整段代码

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

if(age > 18)
"adult"
else
0

// Exiting paste mode, now interpreting.

res12: Any = adult
```

- 默认情况下，`scala`不需要语句终结符，默认每一行作为一条语句

**`print和println`**: `println`多一行换行符

```scala
scala> print("hello world")
hello world
scala> println("hello world")
hello world

scala>
```

**`printf`**:可以用来字符串格式化

**输入语句**

```scala
scala> scala.io.StdIn.readLine()
res24: String = hello

scala> val name = scala.io.StdIn.readLine("please your name:")
please your name:name: String = helllo

scala> 
```

**while do循环**

```scala
scala> var n = 10
n: Int = 10

scala> :paste
// Entering paste mode (ctrl-D to finish)

while(n > 0) {
  println(n)
  n -= 1
}

// Exiting paste mode, now interpreting.

10
9
8
7
6
5
4
3
2
1

scala>
```

**`scala`没有for循环，只能使用简易版for语句**

```scala
scala> var n = 10; for(i <- 1 to n) println(i)
1
2
3
4
5
6
7
8
9
10
n: Int = 10

scala> for(i <- 1 until n) println(i) // until，表达式不达到上限
1
2
3
4
5
6
7
8
9

```

**跳出循环语句**

```scala
scala> import scala.util.control.Breaks._
import scala.util.control.Breaks._

scala> :paste
// Entering paste mode (ctrl-D to finish)

breakable {
    var n = 10
    for(c <- "Hello World") {
        if(n == 5) break;
        print(c)
        n -= 1
    }
}

// Exiting paste mode, now interpreting.

Hello
```

