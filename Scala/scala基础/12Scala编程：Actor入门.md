# Scala编程：Actor入门

- `Scala`的`Actor`类似于`Java`中的多线程编程。但是不同的是，`Scala`的`Actor`提供的模型与多线程有所不同。`Scala`的`Actor`尽可能地避免锁和共享状态，从而避免多线程并发时出现资源争用的情况，进而提升多线程编程的性能。此外，`Scala Actor`的这种模型还可以避免死锁等一系列传统多线程编程的问题。
- `Spark`中使用的分布式多线程框架，是`Akka`。`Akka`也实现了类似`Scala
  Actor`的模型，其核心概念同样也是`Actor`。因此只要掌握了`Scala Actor`，那么在`Spark`源码研究时，至少即可看明白`Akka Actor`相关的代码。但是，换一句话说，由于`Spark`内部有大量的`Akka Actor`的使用，因此对于`Scala Actor`也至少必须掌握，这样才能学习`Spark`源码。

---

## Actor的创建、启动和消息收发

- `Scala`提供了`Actor trait`来让我们更方便地进行`actor`多线程编程，就`Actor trait`就类似于`Java中`的`Thread`和`Runnable`一样，是基础的多线程基类和接口。我们只要重写`Actor trait`的`act`方法，即可实现自己的线程执行体，与`Java`中重写`run`方法类似。
- 此外，使用`start()`方法启动`actor`；使用`!`符号，向`actor`发送消息；`actor`内部使用`receive`和模式匹配接收消息

```scala
// 案例：Actor Hello World
import scala.actors.Actor

class HelloActor extends Actor {
  def act() {
    while (true) {
      receive {
        case name: String => println("Hello, " + name)
      }
    }
  }
}

val helloActor = new HelloActor
helloActor.start()
helloActor ! "leo"

```



---

## 收发case class类型的消息

- `Scala`的`Actor`模型与`Java`的多线程模型之间，很大的一个区别就是，`Scala Actor`天然支持线程之间的精准通信；即一个`actor`可以给其他`actor`直接发送消息。这个功能是非常强大和方便的。
- 要给一个`actor`发送消息，需要使用`“actor ! 消息”`的语法。在`scala`中，通常建议使用样例类，即`case class`来作为消息进行发送。然后在`actor`接收消息之后，可以使用`scala`强大的模式匹配功能来进行不同消息的处理。

```scala
// 案例：用户注册登录后台接口
case class Login(username: String, password: String)
case class Register(username: String, password: String)
class UserManageActor extends Actor {
  def act() {
    while (true) {
      receive {
        case Login(username, password) => println("login, username is " + username + ", password is " + password)
        case Register(username, password) => println("register, username is " + username + ", password is " + password)
      }
    }
  }
}
val userManageActor = new UserManageActor
userManageActor.start()
userManageActor ! Register("leo", "1234"); userManageActor ! Login("leo", "1234")

```



---

## Actor之间互相收发消息

- 如果两个`Actor`之间要互相收发消息，那么`scala`的建议是，一个`actor`向另外一个`actor`发送消息时，同时带上自己的引用；其他`actor`收到自己的消息时，直接通过发送消息的`actor`的引用，即可以给它回复消息。

```scala
// 案例：打电话
case class Message(content: String, sender: Actor)
class LeoTelephoneActor extends Actor {
  def act() {
    while (true) {
      receive {
        case Message(content, sender) => { println("leo telephone: " + content); sender ! "I'm leo, please call me after 10 minutes." }
      }
    }
  }
}
class JackTelephoneActor(val leoTelephoneActor: Actor) extends Actor {
  def act() {
    leoTelephoneActor ! Message("Hello, Leo, I'm Jack.", this)
    receive {
      case response: String => println("jack telephone: " + response)
    }
  }
}

val leoActor = new LeoTelephoneActor
val jackActor = new JackTelephoneActor(leoActor)
leoActor.start()
jackActor.start()
```



---

## 同步消息和Future

- 默认情况下，消息都是异步的；但是如果希望发送的消息是同步的，即对方接受后，一定要给自己返回结果，那么可以使用`!?`的方式发送消息。即`val reply = actor !? message。`
- 如果要异步发送一个消息，但是在后续要获得消息的返回值，那么可以使用`Future`。即`!!`语法。`val future = actor !! message。`
- `val reply = future()。`

