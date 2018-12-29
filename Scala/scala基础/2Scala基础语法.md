## 安装Scala

- 从官网下载[Scala](http://www.scala-lang.org/download/)
- 安装Scala
- windows配置环境变量
- 在终端进行Scala编程

> Scala和Java是可以无缝互操作的。Scala可以任意调用Java的代码。所以Scala与Java的关系是非常非常紧密的

## Scala基础语法

- REPL：Read（取值）-> Evaluation（求值）->Print（打印）-> Loop（循环）。scala解释器也被称为REPL，会快速编译scala代码为字节码，然后交给JVM来执行

- 计算表达式：在scala>命令行内，键入scala代码，解释器会直接返回结果给你。如果你没有指定变量来存放这个值，那么值默认的名称为res，而且会显示结果的数据类型，比如Int、Double、String等等。

  ```scala
  scala > 1 + 1
  res0:Int = 2
  ```

- 内置变量：在后面可以继续使用res这个变量，以及它存放的值。

  ```scala
  scala> 2.0 * res0
  res1: Double = 4.0
  
  scala> "Hi, " + res1
  res2: String = Hi, 4.0
  ```

- 自动补全

- 声明val变量：不能重新赋值

  ```scala
  scala> val result = 1 + 1
  scala> 2 * result
  scala> result = 1 // 出错
  ```

- 声明var变量: 可以重新赋值

  ```scala
  scala> var result = 1 + 1
  scala> 2 * result
  scala> result = 1
  ```

  > 但是在scala程序中，通常建议使用val，也就是常量，因此比如类似于spark的大型复杂系统中，需要大量的网络传输数据，如果使用var，可能会担心值被错误的更改。

- 指定类型

- 声明多个变量

  ```scala
  scala> val num1,num2 = 100
  num1: Int = 100
  num2: Int = 100
  ```

- 基本数据类型：Byte、Char、Short、Int、Long、Float、Double、Boolean

- 类型的加强版类型

- 基本操作符

- 函数调用方式：和python差不多

- apply函数

  ```scala
  scala> "helloworld".apply(5)
  res9: Char = w
  
  scala> "helloworld"(5)
  res10: Char = w
  ```


