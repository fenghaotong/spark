# 重写field的提前定义、Scala继承层级、对象相等性

## 重写field的提前定义

**默认情况下，如果父类中的构造函数代码，用到了会被子类重写的field; 那么出出现令人意想不到的一幕:**

 1. 子类的构造函数（无参）调用父类的构造函数（无参）

 2. 父类的构造函数初始化field（结果正确）

 3. 父类的构造函数使用field执行其他构造代码，但是此时其他构造代码如果使用了该field，而且field要被子类重写，那么它的getter方法被重写，返回0（比如Int）

 4. 子类的构造函数再执行，重写field（结果也正确）

 5. 但是此时子类从父类继承的其他构造代码，已经出现了错误了

    ```scala
    class Student {
    	val classNumber: Int = 10
    	val classScores: Array[Int] = new Array[Int](classNumber)
    }
    
    class PEStudent extends Student {
    	override val classNumber: Int = 3
    }
    ```

    > 本来我们期望的是，PEStudent，可以从Student继承来一个长度为3的classScores数组
    > 结果PEStudent对象，只有一个长度为0的classScores数组
    >
    > 此时只能使用Scala对象继承的一个高级特性: 提前定义，在父类构造函数执行之前，先执行子类的构造函数中的某些代码

    ```scala
    class PEStudent extends {
    	override val classNumber: Int = 3
    } with Student
    ```


## Scala继承层级

- 写的所有的Scala trait和class，都是默认继承自一些Scala根类的，有一些基础的方法
- Scala中，最顶端的两个trait是Nothing和Null，Null trait唯一的对象就是null
  其次是继承了Nothing trait的Any类
- 接着Anyval trait和AnyRef类，都继承自Any类
- Any类是个比较重要的类，其中定义了isInstanceOf和asInstanceOf等方法，以及equals、hashCode等对象的基本方法
- Any类，有点像Java中的Object基类
- AnyRef类，增加了一些多线程的方法，比如wait、notify/notifyAll、synchronized等，也是属于Java Object类的一部分

## 对象相等性

- 在scala中，你如何判断两个引用变量，是否指向同一个对象实例

- AnyRef的eq方法用于检查两个变量是否指向同一个对象实例

- AnyRef的equals方法默认调用eq方法实现，也就是说，默认情况下，判断两个变量相等，要求必须指向同一个对象实例

- 通常情况下，自己可以重写equals方法，根据类的fields来判定是否相等
  此外，定义equals方法时，也最好使用同样的fields，重写hashCode方法

- 如果只是想要简单地通过是否指向同一个对象实例，判定变量是否相当，那么直接使用==操作符即可，默认判断null，然后调用equals方法

  ```scala
  class Product(val name: String, val price: Double) {
  
  	final override def equals(other: Any) = {
  		val that = other.asInstanceOf[Product]
  		if(that == null) false
  		else name == that.name && price == that.price
  	}
  	
  	final override def hashCode = 13 * name.hashCode + 17 * price.hashCode
  	
  }
  ```
