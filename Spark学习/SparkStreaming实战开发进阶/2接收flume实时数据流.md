# 接收flume实时数据流

### flume风格的基于push的方式

Flume被设计为可以在agent之间推送数据，而不一定是从agent将数据传输到sink中。在这种方式下，Spark Streaming需要启动一个作为Avro Agent的Receiver，来让flume可以推送数据过来。下面是我们的整合步骤：

**前提需要**

选择一台机器：

1. Spark Streaming与Flume都可以在这台机器上启动，Spark的其中一个Worker必须运行在这台机器上面
2. Flume可以将数据推送到这台机器上的某个端口

由于flume的push模型，Spark Streaming必须先启动起来，Receiver要被调度起来并且监听本地某个端口，来让flume推送数据。

**配置flume**

在flume-conf.properties文件中，配置flume的sink是将数据推送到其他的agent中

```sh
agent1.sinks.sink1.type = avro
agent1.sinks.sink1.channel = channel1
agent1.sinks.sink1.hostname = 192.168.75.101
agent1.sinks.sink1.port = 8888
```

**配置spark streaming**

在我们的spark工程的pom.xml中加入spark streaming整合flume的依赖

```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-streaming-flume_2.10</artifactId>
    <version>1.5.0</version>
</dependency>
```

在代码中使用整合flume的方式创建输入DStream

[Java代码](src/FlumePushWordCount.java)

```java
import org.apache.spark.streaming.flume.*;

JavaReceiverInputDStream<SparkFlumeEvent> flumeStream =
	FlumeUtils.createStream(streamingContext, [chosen machine's hostname], [chosen port]);
```

> 这里有一点需要注意的是，这里监听的hostname，必须与cluster manager（比如Standalone Master、YARN ResourceManager）是同一台机器，这样cluster manager才能匹配到正确的机器，并将receiver调度在正确的机器上运行。

**部署spark streaming应用**

- 打包工程为一个jar包，使用spark-submit来提交作业

- 启动flume agent

  ```sh
  flume-ng agent -n agent1 -c conf -f /usr/local/flume/conf/flume-conf.properties -Dflume.root.logger=DEBUG,console
  ```


什么时候我们应该用Spark Streaming整合Kafka去用，做实时计算？
什么使用应该整合flume？

看你的实时数据流的产出频率

1. 如果你的实时数据流产出特别频繁，比如说一秒钟10w条，那就必须是kafka，分布式的消息缓存中间件，可以承受超高并发
2. 如果你的实时数据流产出频率不固定，比如有的时候是1秒10w，有的时候是1个小时才10w，可以选择将数据用nginx日志来表示，每隔一段时间将日志文件放到flume监控的目录中，然后呢，spark streaming来计算

### 自定义sink的基于poll的方式

除了让flume将数据推送到spark streaming，还有一种方式，可以运行一个自定义的flume sink

1. Flume推送数据到sink中，然后数据缓存在sink中
2. spark streaming用一个可靠的flume receiver以及事务机制从sink中拉取数据

**前提条件**

1. 选择一台可以在flume agent中运行自定义sink的机器
2. 将flume的数据管道流配置为将数据传送到那个sink中
3. spark streaming所在的机器可以从那个sink中拉取数据

**配置flume**

- 加入sink jars，将以下jar加入flume的classpath中

  1. commons-lang3-3.3.2.jar
  2. spark-streaming-flume-sink_2.10-1.5.0.jar
  3. scala-library-2.10.4.jar

  ```xml
  groupId = org.apache.spark
  artifactId = spark-streaming-flume-sink_2.10
  version = 1.5.0
  
  groupId = org.scala-lang
  artifactId = scala-library
  version = 2.10.4
  
  groupId = org.apache.commons
  artifactId = commons-lang3
  version = 3.3.2
  ```

- 修改配置文件

  ```conf
  agent1.sinks.sink1.type = org.apache.spark.streaming.flume.sink.SparkSink
  agent1.sinks.sink1.hostname = 192.168.75.101
  agent1.sinks.sink1.port = 8888
  agent1.sinks.sink1.channel = channel1
  ```

**配置spark streaming**

```java
import org.apache.spark.streaming.flume.*;

JavaReceiverInputDStream<SparkFlumeEvent>flumeStream =
	FlumeUtils.createPollingStream(streamingContext, [sink machine hostname], [sink port]);
```

[Java代码](src/FlumePollWordCount.java)

**一定要先启动flume，再启动spark streaming**

```sh
flume-ng agent -n agent1 -c conf -f /usr/local/flume/conf/flume-conf.properties -Dflume.root.logger=DEBUG,console
```



