## Scala基础语法：函数

### 函数的定义与调用

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

def sayHello(name:String, age:Int)={
 if (age > 18){
  printf("hi %s, you are a big boy\n", name)
  age
 }
 else{
  printf("hi %s,you are a little boy\n",name)
  age
 }
}

// Exiting paste mode, now interpreting.

sayHello: (name: String, age: Int)Int
scala> sayHello("leo", 30)
hi leo, you are a big boy
res35: Int = 30

```

- 单行函数

```scala
scala > def sayHello(name: String) = print("Hello, " + name)
```

> 函数和函数体之间加等号说明有返回值。

### 函数默认参数和带名参数

```scala
scala> def sayHello(firstName: String, middleName: String = "William", lastName: String = "Croft") = firstName + " " + middleName + " " + lastName
sayHello: (firstName: String, middleName: String, lastName: String)String

scala> sayHello("leo")
res2: String = leo William Croft

scala> sayHello(firstName = "Mick", lastName = "Nina", middleName = "Jack")
res3: String = Mick Jack Nina

scala> sayHello("Mick", lastName = "Nina", middleName = "Jack")
res4: String = Mick Jack Nina

```

> 带名参数可以不按照函数定义的参数顺序来传递参数，而是使用带名参数的方式来传递。还可以混合使用未命名参数和带名参数，但是未命名参数必须排在带名参数前面。

### 可变参数

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

def sum(nums:Int*) = {
var res = 0
for(num <- nums){
res += num
}
res
}
sum(1,2,3,4,5)

// Exiting paste mode, now interpreting.

sum: (nums: Int*)Int
res7: Int = 15

scala> sum(1 to 5:_*)
res8: Int = 15
```

> 使用Scala特殊的语法将参数定义为序列: `sum(1 to 5:_*)`

### 过程、lazy和异常

- 在Scala中，定义函数时，如果函数体直接包裹在了花括号里面，而没有使用=连接，则函数的返回值类型就是Unit。这样的函数就被称之为过程。过程通常用于不需要返回值的函数。
- 在Scala中，提供了lazy值的特性，也就是说，如果将一个变量声明为lazy，则只有在第一次使用该变量时，变量对应的表达式才会发生计算。这种特性对于特别耗时的计算操作特别有用，比如打开文件进行IO，进行网络IO等。
- 即使文件不存在，也不会报错，只有第一个使用变量时会报错，证明了表达式计算的lazy特性

```scala
scala> val lines = fromFile("C://Users//htfeng//Desktop//test.txt").mkString
```

- 异常

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

try {
  throw new IllegalArgumentException("x should not be negative") // 抛出异常
} catch {
  case _: IllegalArgumentException => println("Illegal Argument!") // 捕获异常
} finally {
  print("release resources!")
}

// Exiting paste mode, now interpreting.

Illegal Argument!
release resources!

scala> import java.io._
import java.io._

scala> :paste
// Entering paste mode (ctrl-D to finish)

try {
  throw new IOException("user defined exception")
} catch {
  case e1: IllegalArgumentException => println("illegal argument")
  case e2: IOException => println("io exception")
}

// Exiting paste mode, now interpreting.

io exception

scala>
```



