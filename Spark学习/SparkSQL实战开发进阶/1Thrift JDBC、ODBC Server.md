# Thrift JDBC、ODBC Server

- Spark SQL的Thrift JDBC/ODBC server是基于Hive 0.13的HiveServer2实现的。这个服务启动之后，最主要的功能就是可以让我们通过Java JDBC来以编程的方式调用Spark SQL。此外，在启动该服务之后，可以通过Spark或Hive 0.13自带的beeline工具来进行测试。
- 要启动JDBC/ODBC server，主要执行Spark的sbin目录下的start-thriftserver.sh命令即可
- start-thriftserver.sh命令可以接收所有spark-submit命令可以接收的参数，额外增加的一个参数是--hiveconf，可以用于指定一些Hive的配置属性。可以通过执行./sbin/start-thriftserver.sh --help来查看所有可用参数的列表。默认情况下，启动的服务会在localhost:10000地址上监听请求。

**可以使用两种方式来改变服务监听的地址**

- 第一种：指定环境变量

  ```sh
  export HIVE_SERVER2_THRIFT_PORT=<listening-port>
  export HIVE_SERVER2_THRIFT_BIND_HOST=<listening-host>
  ./sbin/start-thriftserver.sh \
    --master <master-uri> \
    ...
  ```

- 第二种：使用命令的参数

  ```sh
  ./sbin/start-thriftserver.sh \
    --hiveconf hive.server2.thrift.port=<listening-port> \
    --hiveconf hive.server2.thrift.bind.host=<listening-host> \
    --master <master-uri>
    ...
  ```

```sh
hdfs dfs -chmod 777 /tmp/hive-root

./sbin/start-thriftserver.sh \
--jars /usr/local/hive/lib/mysql-connector-java-5.1.17.jar
```

**这两种方式的区别就在于**

- 第一种是针对整个机器上每次启动服务都生效的;
-  第二种仅仅针对本次启动生效

**测试Thrift JDBC/ODBC server**

- 接着就可以通过Spark或Hive的beeline工具来测试Thrift JDBC/ODBC server
- 在Spark的bin目录中，执行beeline命令（当然，我们也可以使用Hive自带的beeline工具）：./bin/beeline
  进入beeline命令行之后，连接到JDBC/ODBC server上去：beeline> !connect jdbc:hive2://localhost:10000
- beeline通常会要求你输入一个用户名和密码。在非安全模式下，我们只要输入本机的用户名（比如root），以及一个空的密码即可。对于安全模式，需要根据beeline的文档来进行认证。

- 除此之外，大家要注意的是，如果我们想要直接通过JDBC/ODBC服务访问Spark SQL，并直接对Hive执行SQL语句，那么就需要将Hive的hive-site.xml配置文件放在Spark的conf目录下。

- Thrift JDBC/ODBC server也支持通过HTTP传输协议发送thrift RPC消息。使用以下方式的配置可以启动HTTP模式：

  1. 命令参数

     ```sh
     ./sbin/start-thriftserver.sh \
       --hive.server2.transport.mode=http \
       --hive.server2.thrift.http.port=10001 \
       --hive.server2.http.endpoint=cliservice \
       --master <master-uri>
       ...
     
     ./sbin/start-thriftserver.sh \
       --jars /usr/local/hive/lib/mysql-connector-java-5.1.17.jar \
       --hiveconf hive.server2.transport.mode=http \
       --hiveconf hive.server2.thrift.http.port=10001 \
       --hiveconf hive.server2.http.endpoint=cliservice 
     ```

  2. beeline连接服务时指定参数

     ```sh
     beeline> !connect jdbc:hive2://localhost:10001/default?hive.server2.transport.mode=http;hive.server2.thrift.http.path=cliservice
     ```

  3. 最重要的，当然是通过Java JDBC的方式，来访问Thrift JDBC/ODBC server，调用Spark SQL，并直接查询Hive中的数据

     ```xml
     <dependency>
       <groupId>org.apache.hive</groupId>
       <artifactId>hive-jdbc</artifactId>
       <version>0.13.0</version>
     </dependency>
     <dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>httpclient</artifactId>
       <version>4.4.1</version>
     </dependency>
     <dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>httpcore</artifactId>
       <version>4.4.1</version>
     </dependency>
     ```

     ```java
     import java.sql.Connection;
     import java.sql.DriverManager;
     import java.sql.PreparedStatement;
     import java.sql.ResultSet;
     
     public class ThriftJDBCServerTest {
     	
     	public static void main(String[] args) {
     		String sql = "select name from users where id=?";
     		
     		Connection conn = null;
     		PreparedStatement pstmt = null;
     		ResultSet rs = null;
     		
     		try {
     			Class.forName("org.apache.hive.jdbc.HiveDriver");  
     			
     			conn = DriverManager.getConnection("jdbc:hive2://192.168.0.103:10001/default?hive.server2.transport.mode=http;hive.server2.thrift.http.path=cliservice", 
     					"root", 
     					"");
     			
     			pstmt = conn.prepareStatement(sql);
     			pstmt.setInt(1, 1);  
     			
     			rs = pstmt.executeQuery();
     			while(rs.next()) {
     				String name = rs.getString(1);
     				System.out.println(name);  
     			}
     		} catch (Exception e) {
     			e.printStackTrace(); 
     		}
     	}
     	
     }
     
     ```

[Java代码](src/ThriftJDBCServerTest.java)