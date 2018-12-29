# 高阶技术之自定义Receiver

- spark streaming可以从任何数据源来接收数据，哪怕是除了它内置支持的数据源以外的其他数据源（比如flume、kafka、socket等）。
- 如果我们想要从spark streaming没有内置支持的数据源中接收实时数据，那么我们需要自己实现一个receiver。

**实现一个自定义的receiver**

- 一个自定义的receiver必须实现以下两个方法：onStart()、onStop()。onStart()和onStop()方法必须不能阻塞数据，一般来说，

- onStart()方法会启动负责接收数据的线程，onStop()方法会确保之前启动的线程都已经停止了。负责接收数据的线程可以调用isStopped()方法来检查它们是否应该停止接收数据。

- 一旦数据被接收了，就可以调用store(data)方法，数据就可以被存储在Spark内部。有一系列的store()重载方法供我们调用，来将数据每次一条进行存储，或是每次存储一个集合或序列化的数据。

- 接收线程中的任何异常都应该被捕获以及正确处理，从而避免receiver的静默失败。restart()方法会通过异步地调用onStop()和onStart()方法来重启receiver。stop()方法会调用onStop()方法来停止receiver。reportError()方法会汇报一个错误消息给driver，但是不停止或重启receiver。

  ```java
  package cn.spark.study.streaming.upgrade;
  
  import java.io.BufferedReader;
  import java.io.InputStreamReader;
  import java.net.ConnectException;
  import java.net.Socket;
  
  import org.apache.spark.storage.StorageLevel;
  import org.apache.spark.streaming.receiver.Receiver;
  
  public class JavaCustomReceiver extends Receiver<String> {
  
  	  private static final long serialVersionUID = 3184893502610667539L;
  	  String host = null;
  	  int port = -1;
  
  	  public JavaCustomReceiver(String host_ , int port_) {
  	    super(StorageLevel.MEMORY_AND_DISK_2());
  	    host = host_;
  	    port = port_;
  	  }
  
  	  public void onStart() {
  	    // Start the thread that receives data over a connection
  	    new Thread()  {
  	      @Override public void run() {
  	        receive();
  	      }
  	    }.start();
  	  }
  
  	  public void onStop() {
  
  	  }
  
  	  /** Create a socket connection and receive data until receiver is stopped */
  	  private void receive() {
  	    Socket socket = null;
  	    String userInput = null;
  
  	    try {
  	      // connect to the server
  	      socket = new Socket(host, port);
  
  	      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  
  	      // Until stopped or connection broken continue reading
  	      while (!isStopped() && (userInput = reader.readLine()) != null) {
  	        System.out.println("Received data '" + userInput + "'");
  	        store(userInput);
  	      }
  	      reader.close();
  	      socket.close();
  
  	      // Restart in an attempt to connect again when server is active again
  	      restart("Trying to connect again");
  	    } catch(ConnectException ce) {
  	      // restart if could not connect to server
  	      restart("Could not connect", ce);
  	    } catch(Throwable t) {
  	      // restart if there is any other error
  	      restart("Error receiving data", t);
  	    }
  	  }
  	}
  
  ```


**在spark streaming中使用自定义的receiver**

```java
JavaDStream<String> lines = jssc.receiverStream(new JavaCustomReceiver(host, port));
```

[Java代码](src/CustomReceiverWordCount.java)

- 安装网络服务工具

  ```sh
  wget http://vault.centos.org/6.3/os/i386/Packages/nc-1.84-22.el6.i686.rpm
  rpm -iUv nc-1.84-22.el6.i686.rpm
  nc -lk 9999
  ```
