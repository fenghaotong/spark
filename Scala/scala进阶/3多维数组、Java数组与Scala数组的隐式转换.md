# 多维数组、Java数组与Scala数组的隐式转换

## 多维数据

- 什么是多维数组？：数组的元素，还是数组，数组套数组，就是多维数组

- 构造指定行与列的二维数组：Array.ofDim方法

  ```scala
  val mutilDimArr1 = Array.ofDim[Double](3, 4)
  multiDimArr1(0)(0) = 1.0
  ```

- 构造不规则多维数组

  ```scala
  val multiDimArr2 = new Array[Array[Int]](3)
  multiDimArr2(0) = new Array[Int] (1)
  multiDimArr2(1) = new Array[Int] (2)
  multiDimArr2(2) = new Array[Int] (3)
  multiDimArr2(1)(1) = 1
  ```


## Java数组与Scala数组的隐式转换

- Scala代码中，直接调用JDK（Java）的API，比如调用一个Java类的方法，势必可能会传入Java类型的list；Scala中构造出来的list，其实是ArrayBuffer；你直接把Scala的ArrayBuffer传入Java接收ArrayList的方法，肯定不行。

  ```scala
  import scala.collection.JavaConversions.bufferAsJavaList
  import scala.collection.mutable.ArrayBuffer
  
  val command = ArrayBuffer("javac", "src\\HelloWorld.java")
  val processBuilder = new ProcessBuilder(command)
  val process = processBuilder.start()
  val res = process.waitFor()
  
  import scala.collection.JavaConversions.asScalaBuffer
  import scala.collection.mutable.Buffer
  
  val cmd: Buffer[String] = processBuilder.command()
  
  ```

  ```java
  public class HelloWorld {
  	
  	public static void main(String[] args) {
  		System.out.println("Hello World");
  	}	
  }
  ```
