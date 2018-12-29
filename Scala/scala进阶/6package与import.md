# package与import

## 为什么要有package的概念？

- 因为要对多个同名的类进行命名空间的管理，避免同名类发生冲突
- 比如说，scala.collection.mutable.Map和scala.collection.immutable.Map

### package定义

- package定义的第一种方式: 多层级package定义（比较差的做法，一般不这么干）

  ```scala
  package com {
  	package ibeifeng {
  		package scala {
  			class Test {}
  		}
  	}
  }
  ```

- package定义的第二种方式: 串联式package定义（也不怎么样，一般也不这么干）

  ```scala
  package com.ibeifeng.scala {
  	package service {
  		class Test {}
  	}
  }
  ```

- package定义的第三种方式: 文件顶部package定义

  ```scala
  package com.ibeifeng.scala.service
  
  class Test {
  
  }
  ```

- package定义的第四种方式: IDE自动生成包

### package特性

- package特性一: 同一个包定义，可以在不同的scala源文件中的; 一个scala源文件内，可以包含两个包

  ```scala
  // Test1.scala
  package com {
  	package ibeifeng {
  		package scala {
  			class Test1
  		}
  	}
  }
  
  // Test2.scala
  package com {
  	package ibeifeng {
  		package scala {
  			class Test2
  		}
  	}
  }
  
  // Test3.scala
  package com {
  	package ibeifeng {
  		package scala1 {
  			class Test
  		}
  	}
  }
  
  package com {
  	package ibeifeng {
  		package scala2 {
  			class Test
  		}
  	}
  }
  ```

- package特性二: 子包中的类，可以访问父包中的类

  ```scala
  //  Test.scala
  package com {
  	package ibeifeng {
  		package scala {
  			object Utils {
  				def isNotEmpty(str: String): Boolean = str != null && str != ""
  			}
  			
  			class Test
  		
  			package service {
  				class MyService {
  					def sayHello(name: String) {
  						if(Utils.isNotEmpty(name)) {
  							println("Hello, " + name)
  						} else {
  							println("Who are you?")  
  						}
  					}
  				}
  			}
  		}
  	}
  }
  
  object MainClass {
    def main(args: Array[String]): Unit = {
      val service = new com.ibeifeng.scala.service.MyService
      service.sayHello("leo")  
      service.sayHello("")  
    }
  }
  ```

- package特性三: 相对包名与绝对包名

  ```scala
  package com {
  	package ibeifeng {
  		package scala {
  			object Utils {
  				def isNotEmpty(str: String): Boolean = str != null && str != ""
  			}
  			
  			class Test
  			
  			package collection {}
  			
  			package service {
  				class MyService {
  					// 这会报错，默认使用相对报名，从com.ibeifeng.scala.collection包中，寻找mutable包下的ArrayBuffer类
  					// 但是找不到，所以会报错
  					// val names = new scala.collection.mutable.ArrayBuffer[String]
  					
  					// 正确的做法是使用_root_，引用绝对包名
  					val names = new _root_.scala.collection.mutable.ArrayBuffer[String]
  					
  					def sayHello(name: String) {
  						if(Utils.isNotEmpty(name)) {
  							println("Hello, " + name)
  						} else {
  							println("Who are you?")  
  						}
  					}
  				}
  			}
  		}
  	}
  }
  ```

- package特性四: 定义package对象（比较少）

  package内的成员，可以直接访问package对象内的成员

  ```scala
  package com.ibeifeng.scala
  
  package object service {
  	val defaultName = "Somebody"
  }
  
  package service {
  	class MyService {
  		def sayHello(name: String) {
  			if(name != null && name != "") {
  				println("Hello, " + name)
  			} else {
  				println("Hello, " + defaultName) 
  			}
  		}
  	}
  }
  ```

- package特性五: package可见性

  ```scala
  package com.ibeifeng.scala
  
  class Person {
  	private[scala] val name = "leo"
  	private[ibeifeng] val age = 25
  }
  ```

## import

- 如果没有import，那么。。。你每次创建某个包下的类的对象，都得用`new com.ibeifeng.scala.service.MyService`这种冗长的格式

- 如果用了import，只要先`import com.ibeifeng.scala.service.MyService`，然后再new MyService，即可

  ```scala
  import com.ibeifeng.scala.service.MyService;
  
  object MainClass {
    def main(args: Array[String]): Unit = {
      val service = new MyService
      service.sayHello("leo")  
      service.sayHello("")  
    }
  }
  ```

### import特性

- import特性一: 用import com.ibeifeng.scala.service._这种格式，可以导入包下所有的成员

- import特性二: scala与java不同之处在于，任何地方都可以使用import，比如类内、方法内，这种方式的好处在于，可以在一定作用域范围内使用导入

  ```scala
  object MainClass {
    def main(args: Array[String]): Unit = {
      import com.ibeifeng.scala.service._
      
      val service = new MyService
      service.sayHello("leo")  
      service.sayHello("")  
    }
  }
  ```

- import特性三: 选择器、重命名、隐藏

  ```scala
  import com.ibeifeng.scala.service.{ MyService } // 仅仅导入com.ibeifeng.scala.service包下的MyService类
  import com.ibeifeng.scala.service.{ MyService => MyServiceImpl } // 将导入的类进行重命名
  import com.ibeifeng.scala.service.{ MyService => _, _ } // 导入com.ibeifeng.scala.service包下所有的类，但是隐藏掉MyServicep类
  ```

- import特性四: 隐式导入

  每个scala程序默认都会隐式导入以下几个包下所有的成员

  ```scala
  import java.lang._
  import scala._
  import Predef._
  ```
