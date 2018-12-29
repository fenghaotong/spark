# Scala编程：面向对象

## 类
### 定义一个简单的类

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

class HelloWorld {
  private var name = "leo"
  def sayHello() { print("Hello, " + name) }
  def getName = name
}

// Exiting paste mode, now interpreting.

defined class HelloWorld

scala> var helloWorld = new HelloWorld
helloWorld: HelloWorld = HelloWorld@383d9653

scala> helloWorld.sayHello()
Hello, leo

scala> helloWorld.getName // 也可以不加括号，如果定义方法时不带括号，则调用方法时也不能带括号

res83: String = leo
```

### get与setter

- 定义不带`private的var field`，此时scala生成的面向JVM的类时，会定义为`private的name`字段，并提供`public`的`getter和setter`方法

- 而如果使用`private`修饰`field`，则生成的`getter和setter`也是`private`的

- 如果定义`val field`，则只会生成getter方法

- 如果不希望生成`setter和getter`方法，则将field声明为`private[this]`
- 调用`getter和setter`方法，分别叫做`name和name_ =`

### 自定义getter与setter方法

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

class Student {
  private var myName = "leo"
  def name = "your name is " + myName
  def name_=(newValue: String)  {
    print("you cannot edit your name!!!")
  }
}


// Exiting paste mode, now interpreting.

defined class Student

scala> val leo = new Student
leo: Student = Student@77c66a4f

scala> leo.name
res0: String = your name is leo

scala> leo.name = "leo1"
you cannot edit your name!!!leo.name: String = your name is leo

```

### private[this]的使用

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

class Student{
private[this] var myAge = 0
def age_= (newAge:Int){
if(newAge > 0) myAge = newAge
else print("illegal age")
}
def age = myAge
def older(s: Student)={
myAge > s.myAge
}
}

// Exiting paste mode, now interpreting.

<console>:19: error: value myAge is not a member of Student
       myAge > s.myAge
                 ^

```

### Java风格的getter和setter方法

- Scala的getter和setter方法的命名与java是不同的，是field和field_=的方式

- 如果要让scala自动生成java风格的getter和setter方法，只要给`field`添加`@BeanProperty`注解即可

- 此时会生成4个方法，`name: String、name_=(newValue: String): Unit、getName(): String、setName(newValue: String): Unit`

```scala
setName(newValue: String): Unit
import scala.reflect.BeanProperty
class Student {
  @BeanProperty var name: String = _
}
class Student(@BeanProperty var name: String)

val s = new Student
s.setName("leo")
s.getName()

```

### 辅助构造函数

Scala中，可以给类定义多个辅助constructor，类似于java中的构造函数重载

辅助构造函数之间可以互相调用，而且必须第一行调用主构造函数

```scala
class Student {
  private var name = ""
  private var age = 0
  def this(name: String) {
    this()
    this.name = name
  }
  def this(name: String, age: Int) {
    this(name)
    this.age = age
  }

```

### 主构造函数

- Scala中，主constructor是与类名放在一起的，与java不同

- 而且类中，没有定义在任何方法或者是代码块之中的代码，就是主constructor的代码，这点感觉没有java那么清晰

```scala
class Student(val name: String, val age: Int) {
  println("your name is " + name + ", your age is " + age)
}

class Student(val name: String = "leo", val age: Int = 30) {
  println("your name is " + name + ", your age is " + age)
}

```

### 内部类

```scala
import scala.collection.mutable.ArrayBuffer
class Class {
  class Student(val name: String) {}
  val students = new ArrayBuffer[Student]
  def getStudent(name: String) =  {
    new Student(name)
  }
}

val c1 = new Class
val s1 = c1.getStudent("leo")
c1.students += s1

val c2 = new Class
val s2 = c2.getStudent("leo")
c1.students += s2

```
## 对象

### Scala特有对象object

- object，相当于class的单个实例，通常在里面放一些**静态的**`field或者method`
- 第一次调用`object`的方法时，就会执行`object的constructor`，也就是`object`内部不在`method`中的代码；但是`object`不能定义接受参数的`constructor`
- 注意，`object`的`constructor`只会在其第一次被调用时执行一次，以后再次调用就不会再次执行`constructor`了
- `object`通常用于作为单例模式的实现，或者放`class`的静态成员，比如工具方法

```scala
object Person {
  private var eyeNum = 2
  println("this Person object!")
  def getEyeNum = eyeNum
}

```

### 伴生对象

- 如果有一个`class`，还有一个与`class`同名的`object`，那么就称这个`object`是`class`的伴生对象，`class`是`object`的伴生类
- 伴生类和伴生对象必须存放在一个`.scala`文件之中
- 伴生类和伴生对象，最大的特点就在于，互相可以访问`private field`

```scala
object Person {
  private val eyeNum = 2
  def getEyeNum = eyeNum
}

class Person(val name: String, val age: Int) {
  def sayHello = println("Hi, " + name + ", I guess you are " + age + " years old!" + ", and usually you must have " + Person.eyeNum + " eyes.")
}

```

### 让object继承抽象类

- `object`的功能其实和class类似，除了不能定义接受参数的`constructor`之外
- `object`也可以继承抽象类，并覆盖抽象类中的方法

```scala
abstract class Hello(var message: String) {
  def sayHello(name: String): Unit
}

object HelloImpl extends Hello("hello") {
  override def sayHello(name: String) = {
    println(message + ", " + name)
  }
}

```

### apply方法

- `object`中非常重要的一个特殊方法，就是`apply`方法
- 通常在伴生对象中实现`apply`方法，并在其中实现构造伴生类的对象的功能
- 而创建伴生类的对象时，通常不会使用`new Class`的方式，而是使用Class()的方式，隐式地调用伴生对象得`apply`方法，这样会让对象创建更加简洁

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

class Person(val name: String)
object Person {
  def apply(name: String) = new Person(name)
}

// Exiting paste mode, now interpreting.

defined class Person
defined object Person

scala> val s1 = new Person("leo")
s1: Person = Person@27959c4e

scala> val s2 = Person("jack")
s2: Person = Person@95f61c2

```

### main方法

- 就如同`java`中，如果要运行一个程序，必须编写一个包含main方法类一样；在`scala`中，如果要运行一个应用程序，那么必须有一个`main`方法，作为入口
- `scala`中的`main`方法定义为`def main(args:Array[String])`，而且必须定义在`object`中

````scala
object HelloWorld {
  def main(args: Array[String]) {
    println("Hello World!!!")
  }
}

````

- 除了自己实现`main`方法之外，还可以继承`App Trait`，然后将需要在`main`方法中运行的代码，直接作为`object`的`constructor`代码；而且用`args`可以接受传入的参数

```scala
object HelloWorld extends App {
  if (args.length > 0) println("hello, " + args(0))
  else println("Hello World!!!")
}

```

编译执行

```scala
scalac HelloWorld.scala
scala -Dscala.time HelloWorld
scala -Dscala.time HelloWorld leo
```

### 用object来实现枚举功能

- `Scala`没有直接提供类似于`Java`中的`Enum`这样的枚举特性，如果要实现枚举，则需要用`object`继承`Enumeration`类，并且调用`Value`方法来初始化枚举值

```scala
object Season extends Enumeration {
  val SPRING, SUMMER, AUTUMN, WINTER = Value
}

```

- 还可以通过`Value`传入枚举值的`id和name`，通过`id和toString`可以获取;还可以通过`id和name`来查找枚举值

```scala
object Season extends Enumeration {
  val SPRING = Value(0, "spring")
  val SUMMER = Value(1, "summer")
  val AUTUMN = Value(2, "autumn")
  val WINTER = Value(3, "winter")
}
Season(0)
Season.withName("spring")

```

- 使用枚举`object.values`可以遍历枚举值

```scala
for (ele <- Season.values) println(ele)

```

## 继承

- Scala中，让子类继承父类，与`Java`一样，也是使用`extends`关键字
- 继承就代表，子类可以从父类继承父类的`field和method`；然后子类可以在自己内部放入父类所没有，子类特有的`field和method`；使用继承可以有效复用代码
- 子类可以覆盖父类的`field和method`；但是如果父类用`final`修饰，`field和method`用f`inal`修饰，则该类是无法被继承的，`field和method`是无法被覆盖的

```scala
class Person {
  private var name = "leo"
  def getName = name
}
class Student extends Person {
  private var score = "A"
  def getScore = score
}

```

### override和super

- `Scala`中，如果子类要覆盖一个父类中的非抽象方法，则必须使用`override`关键字
- `override`关键字可以帮助我们尽早地发现代码里的错误，比如：`override`修饰的父类方法的方法名我们拼写错了；比如要覆盖的父类方法的参数我们写错了；等等
- 此外，在子类覆盖父类方法之后，如果我们在子类中就是要调用父类的被覆盖的方法呢？那就可以使用`super`关键字，显式地指定要调用父类的方法

```scala
class Person {
  private var name = "leo"
  def getName = name
}
class Student extends Person {
  private var score = "A"
  def getScore = score
  override def getName = "Hi, I'm " + super.getName
}

```

### override field

- `Scala`中，子类可以覆盖父类的v`al field`，而且子类的`val
  field`还可以覆盖父类的`val field的getter`方法；只要在子类中使用`override`关键字即可

```scala
class Person {
  val name: String = "Person"
  def age: Int = 0
}

class Student extends Person {
  override val name: String = "leo"
  override val age: Int = 30
}

```

### 父类和子类的类型判断和转换（ isInstanceOf和asInstanceOf）

- 如果我们创建了子类的对象，但是又将其赋予了父类类型的变量。则在后续的程序中，我们又需要将父类类型的变量转换为子类类型的变量，应该如何做？
- 首先，需要使用`isInstanceOf`判断对象是否是指定类的对象，如果是的话，则可以使用`asInstanceOf`将对象转换为指定类型
- 注意，如果对象是`null`，则`isInstanceOf`一定返回`false`，`asInstanceOf`一定返回null
- 注意，如果没有用`isInstanceOf`先判断对象是否为指定类的实例，就直接用`asInstanceOf`转换，则可能会抛出异常

```scala
class Person
class Student extends Person
val p: Person =  new Student
var s: Student = null
if (p.isInstanceOf[Student]) s = p.asInstanceOf[Student]

```

### getClass和classOf

- `isInstanceOf`只能判断出对象是否是指定类以及其子类的对象，而不能精确判断出，对象就是指定类的对象
- 如果要求精确地判断对象就是指定类的对象，那么就只能使用`getClass和classOf`了
- 对象`.getClass`可以精确获取对象的类，`classOf[类]`可以精确获取类，然后使用==操作符即可判断

```scala
class Person
class Student extends Person
val p: Person = new Student
p.isInstanceOf[Person]
p.getClass == classOf[Person]
p.getClass == classOf[Student]

```

### protected

- 跟`java`一样，`scala`中同样可以使用`protected`关键字来修饰`field和method`，这样在子类中就不需要`super`关键字，直接就可以访问`field和method`
- 还可以使用`protected[this]`，则只能在当前子类对象中访问父类的`field和method`，无法通过其他子类对象访问父类的`field和method`

```scala
class Person {
  protected var name: String = "leo"
  protected[this] var hobby: String = "game"
} 
class Student extends Person {
  def sayHello = println("Hello, " + name)
  def makeFriends(s: Student) {
    println("my hobby is " + hobby + ", your hobby is " + s.hobby)
  }
}
```

### 调用父类的constructor

- Scala中，每个类可以有一个主`constructor`和任意多个辅助`constructor`，而每个辅助`constructor`的第一行都必须是调用其他辅助`constructor`或者是主`constructor`；因此子类的辅助`constructor`是一定不可能直接调用父类的constructor的
- 只能在子类的主`constructor`中调用父类的`constructor`，以下这种语法，就是通过子类的主构造函数来调用父类的构造函数
- 注意！如果是父类中接收的参数，比如`name和age`，子类中接收时，就不要用任何`val或var`来修饰了，否则会认为是子类要覆盖父类的`field`

```scala
class Person(val name: String, val age: Int)
class Student(name: String, age: Int, var score: Double) extends Person(name, age) {
  def this(name: String) {
    this(name, 0, 0)
  }
  def this(age: Int) {
    this("leo", age, 0)
  }
}

```

### 匿名内部类

- 在`Scala`中，匿名子类是非常常见，而且非常强大的。`Spark`的源码中也大量使用了这种匿名子类。
- 匿名子类，也就是说，可以定义一个类的没有名称的子类，并直接创建其对象，然后将对象的引用赋予一个变量。之后甚至可以将该匿名子类的对象传递给其他函数。

```scala
class Person(protected val name: String) {
  def sayHello = "Hello, I'm " + name
}
// 匿名内部类
val p = new Person("leo") {
  override def sayHello = "Hi, I'm " + name
}
def greeting(p: Person { def sayHello: String }) {
  println(p.sayHello)
}

```

### 抽象类

- 如果在父类中，有某些方法无法立即实现，而需要依赖不同的子来来覆盖，重写实现自己不同的方法实现。此时可以将父类中的这些方法不给出具体的实现，只有方法签名，这种方法就是抽象方法。
- 而一个类中如果有一个抽象方法，那么类就必须用`abstrac`t来声明为抽象类，此时抽象类是不可以实例化的
- 在子类中覆盖抽象类的抽象方法时，不需要使用`override`关键字

```scala
abstract class Person(val name: String) {
  def sayHello: Unit
}
class Student(name: String) extends Person(name) {
  def sayHello: Unit = println("Hello, " + name)
}

```

### 抽象field

- 如果在父类中，定义了`field`，但是没有给出初始值，则此`field`为抽象`field`
- 抽象`field`意味着，`scala`会根据自己的规则，为`var`或`val`类型的`field`生成对应的`getter`和`setter`方法，但是父类中是没有该`field`的
- 子类必须覆盖`field`，以定义自己的具体`field`，并且覆盖抽象`field`，不需要使用`override`关键字

```scala
abstract class Person {
  val name: String
}

class Student extends Person {
  val name: String = "leo"
}

```

## Trait

### Train基础知识

#### 将trait作为接口使用

- `Scala`中的`Triat`是一种特殊的概念
- 首先我们可以将`Trait`作为接口来使用，此时的`Triat`就与`Java`中的接口非常类似
- 在`triat`中可以定义抽象方法，就与抽象类中的抽象方法一样，只要不给出方法的具体实现即可
- 类可以使用`extends`关键字继承`trait`，注意，这里不是`implement`，而是`extends`，在scala中没有`implement`的概念，无论继承类还是`trait`，统一都是`extends`
- 类继承`trait`后，必须实现其中的抽象方法，实现时不需要使用`override`关键字
- `scala`不支持对类进行多继承，但是支持多重继承`trait`，使用`with`关键字即可

```scala
trait HelloTrait {
  def sayHello(name: String)
}
trait MakeFriendsTrait {
  def makeFriends(p: Person)
}
class Person(val name: String) extends HelloTrait with MakeFriendsTrait with Cloneable with Serializable {
  def sayHello(name: String) = println("Hello, " + name)
  def makeFriends(p: Person) = println("Hello, my name is " + name + ", your name is " + p.name)
}

```



#### 在trait中定义具体方法

- `Scala`中的`Triat`可以不是只定义抽象方法，还可以定义具体方法，此时`trait`更像是包含了通用工具方法的东西
- 有一个专有的名词来形容这种情况，就是说trait的功能混入了类
- 举例来说，`trait`中可以包含一些很多类都通用的功能方法，比如打印日志等等，`spark`中就使用了trait来定义了通用的日志打印方法

```scala
trait Logger {
  def log(message: String) = println(message)
}

class Person(val name: String) extends Logger {
  def makeFriends(p: Person) {
    println("Hi, I'm " + name + ", I'm glad to make friends with you, " + p.name)
    log("makeFriends methdo is invoked with parameter Person[name=" + p.name + "]")
  }
}

```



#### 在trait中定义具体字段

- `Scala`中的`Triat`可以定义具体`field`，此时继承`trait`的类就自动获得了`trait`中定义的`field`
- 但是这种获取`field`的方式与继承`class`是不同的：如果是继承`class`获取的field，实际是定义在父类中的；而继承`trait`获取的`field`，就直接被添加到了类中

```scala
trait Person {
  val eyeNum: Int = 2
}

class Student(val name: String) extends Person {
  def sayHello = println("Hi, I'm " + name + ", I have " + eyeNum + " eyes.")
}

```



#### 在trait中定义抽象字段

- `Scala`中的`Triat`可以定义抽象`field`，而`trait`中的具体方法则可以基于抽象`field`来编写
- 但是继承`trait`的类，则必须覆盖抽象`field`，提供具体的值

```scala
trait SayHello {
  val msg: String
  def sayHello(name: String) = println(msg + ", " + name)
}

class Person(val name: String) extends SayHello {
  val msg: String = "hello"
  def makeFriends(p: Person) {
    sayHello(p.name)
    println("I'm " + name + ", I want to make friends with you!")
  }
}

```



### trait高级知识

#### 为实例对象混入trait

- 有时我们可以在创建类的对象时，指定该对象混入某个trait，这样，就只有这个对象混入该trait的方法，而类的其他对象则没有

```scala
trait Logged {
  def log(msg: String) {}
}
trait MyLogger extends Logged {
  override def log(msg: String) { println("log: " + msg) }
}  
class Person(val name: String) extends Logged {
    def sayHello { println("Hi, I'm " + name); log("sayHello is invoked!") }
}

val p1 = new Person("leo")
p1.sayHello
val p2 = new Person("jack") with MyLogger
p2.sayHello
```

#### trait调用链

- `Scala`中支持让类继承多个`trait`后，依次调用多个`trait`中的同一个方法，只要让多个`trait`的同一个方法中，在最后都执行`super.`方法即可
- 类中调用多个`trait`中都有的这个方法时，首先会从最右边的`trait`的方法开始执行，然后依次往左执行，形成一个调用链条
- 这种特性非常强大，其实就相当于设计模式中的责任链模式的一种具体实现依赖

```scala
trait Handler{
  def handler(data:String){}
}

trait DtaValidHandler extends Handler{
  override def handle(data: String) {
    println("check data: " + data)
    super.handle(data)
  } 
}

trait SignatureValidHandler extends Handler {
  override def handle(data: String) {
    println("check signature: " + data)
    super.handle(data)
  }
}
class Person(val name: String) extends SignatureValidHandler with DataValidHandler {
  def sayHello = { println("Hello, " + name); handle(name) }
}
```

####  在trait中覆盖抽象方法

- 在`trait`中，是可以覆盖父`trait`的抽象方法的
- 但是覆盖时，如果使用了`super.`方法的代码，则无法通过编译。因为`super.`方法就会去掉用父`trait`的抽象方法，此时子`trait`的该方法还是会被认为是抽象的
- 此时如果要通过编译，就得给子`trait`的方法加上`abstract override`修饰

```scala
trait Logger {
  def log(msg: String)
}

trait MyLogger extends Logger {
  abstract override def log(msg: String) { super.log(msg) }
}

```



#### 混合使用trait的具体方法和抽象方法

- 在`trait`中，可以混合使用**具体方法和抽象方法**
- 可以让具体方法依赖于抽象方法，而抽象方法则放到继承`trait`的类中去实现
- 这种`trait`其实就是设计模式中的模板设计模式的体现

```scala
trait Valid {
  def getName: String
  def valid: Boolean = {
    getName == "leo"    
  }
}
class Person(val name: String) extends Valid {
  println(valid)
  def getName = name
}

```



####  trait的构造机制

- 在`Scala`中，`trait`也是有构造代码的，也就是`trait`中的，不包含在任何方法中的代码
- 而继承了`trait`的类的构造机制如下：1、父类的构造函数执行；2、`trait`的构造代码执行，多个`trait`从左到右依次执行；3、构造`trait`时会先构造父`trait`，如果多个`trait`继承同一个父`trait`，则父`trait`只会构造一次；4、所有`trait`构造完毕之后，子类的构造函数执行

```scala
class Person { println("Person's constructor!") }
trait Logger { println("Logger's constructor!") }
trait MyLogger extends Logger { println("MyLogger's constructor!") }
trait TimeLogger extends Logger { println("TimeLogger's constructor!") }
class Student extends Person with MyLogger with TimeLogger {
  println("Student's constructor!")
}

```



#### trait字段的初始化

- 在`Scala`中，`trait`是没有接收参数的构造函数的，这是`trait`与`class`的唯一区别，但是如果需求就是要`trait`能够对`field`进行初始化，该怎么办呢？只能使用`Scala`中非常特殊的一种高级特性——提前定义

```scala
trait SayHello {
  val msg: String
  println(msg.toString)
}

class Person

val p = new {
  val msg: String = "init"
} with Person with SayHello

class Person extends {
  val msg: String = "init"
} with SayHello {}

// 另外一种方式就是使用lazy value
trait SayHello {
  lazy val msg: String = null
  println(msg.toString)
}
class Person extends SayHello {
  override lazy val msg: String = "init"
}

```



#### 让trait继承类

- `在Scala`中，`trait`也可以继承自`class`，此时这个`class`就会成为所有继承该`trait`的类的父类

```scala
class MyUtil {
  def printMessage(msg: String) = println(msg)
}

trait Logger extends MyUtil {
  def log(msg: String) = printMessage("log: " + msg)
}

class Person(val name: String) extends Logger {
  def sayHello {
    log("Hi, I'm " + name)
    printMessage("Hi, I'm " + name)
  }
}

```



