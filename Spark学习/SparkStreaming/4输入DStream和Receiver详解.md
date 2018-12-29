# 输入DStream和Receiver详解

## 原理介绍

- 输入DStream代表了来自数据源的输入数据流。在之前的wordcount例子中，lines就是一个输入DStream（JavaReceiverInputDStream），代表了从netcat（nc）服务接收到的数据流。除了文件数据流之外，所有的输入DStream都会绑定一个Receiver对象，该对象是一个关键的组件，用来从数据源接收数据，并将其存储在Spark的内存中，以供后续处理。
- Spark Streaming提供了两种内置的数据源支持；
  1. 基础数据源：StreamingContext API中直接提供了对这些数据源的支持，比如文件、socket、Akka Actor等。
  2. 高级数据源：诸如Kafka、Flume、Kinesis、Twitter等数据源，通过第三方工具类提供支持。这些数据源的使用，需要引用其依赖。
  3. 自定义数据源：我们可以自己定义数据源，来决定如何接受和存储数据。

- 要注意的是，如果你想要在实时计算应用中并行接收多条数据流，可以创建多个输入DStream。这样就会创建多个Receiver，从而并行地接收多个数据流。但是要注意的是，一个Spark Streaming Application的Executor，是一个长时间运行的任务，因此，它会独占分配给Spark Streaming Application的cpu core。从而只要Spark Streaming运行起来以后，这个节点上的cpu core，就没法给其他应用使用了。
- 使用本地模式，运行程序时，绝对不能用local或者local[1]，因为那样的话，只会给执行输入DStream的executor分配一个线程。而Spark Streaming底层的原理是，至少要有两条线程，一条线程用来分配给Receiver接收数据，一条线程用来处理接收到的数据。因此必须使用local[n]，n>=2的模式。
- 如果不设置Master，也就是直接将Spark Streaming应用提交到集群上运行，那么首先，必须要求集群节点上，有>1个cpu core，其次，给Spark Streaming的每个executor分配的core，必须>1，这样，才能保证分配到executor上运行的输入DStream，两条线程并行，一条运行Receiver，接收数据；一条处理数据。否则的话，只会接收数据，不会处理数据。
- 因此，基于此，特此声明，我们本系列课程所有的练习，都是基于local[2]的本地模式，因为我们的虚拟机上都只有一个1个cpu core。但是大家在实际企业工作中，机器肯定是不只一个cpu core的，现在都至少4核了。到时记得给每个executor的cpu core，设置为超过1个即可。

![](img\Receiver和cpu core分配说明.png)

## 输入DStream之基础数据源以及基于HDFS的实时wordcount程序

- Socket：之前的wordcount例子，已经演示过了，StreamingContext.socketTextStream()

- HDFS文件

  基于HDFS文件的实时计算，其实就是，监控一个HDFS目录，只要其中有新文件出现，就实时处理。相当于处理实时的文件流。

```scala
streamingContext.fileStream<KeyClass, ValueClass, InputFormatClass>(dataDirectory)

streamingContext.fileStream[KeyClass, ValueClass, InputFormatClass](dataDirectory)
```

- Spark Streaming会监视指定的HDFS目录，并且处理出现在目录中的文件。要注意的是，所有放入HDFS目录中的文件，都必须有相同的格式；必须使用移动或者重命名的方式，将文件移入目录；一旦处理之后，文件的内容即使改变，也不会再处理了；基于HDFS文件的数据源是没有Receiver的，因此不会占用一个cpu core。

- 基于HDFS的实时wordcount程序

  [Java版本](src/java/HDFSWordCount.java)

  [Scala版本](src/scala/HDFSWordCount.scala)

## 输入DStream之Kafka数据源实战（基于Receiver的方式）

### 基于Receiver的方式

- 这种方式使用Receiver来获取数据。Receiver是使用Kafka的高层次Consumer API来实现的。receiver从Kafka中获取的数据都是存储在Spark Executor的内存中的，然后Spark Streaming启动的job会去处理那些数据。
- 然而，在默认的配置下，这种方式可能会因为底层的失败而丢失数据。如果要启用高可靠机制，让数据零丢失，就必须启用Spark Streaming的预写日志机制（Write Ahead Log，WAL）。该机制会同步地将接收到的Kafka数据写入分布式文件系统（比如HDFS）上的预写日志中。所以，即使底层节点出现了失败，也可以使用预写日志中的数据进行恢复。

### 如何进行Kafka数据源连接

- 在maven的pom.xml中添加依赖

  ```sh
  groupId = org.apache.spark
  artifactId = spark-streaming-kafka_2.10
  version = 1.5.0
  ```

- 使用第三方工具类创建输入DStream

  ```scala
  JavaPairReceiverInputDStream<String, String> kafkaStream = 
  KafkaUtils.createStream(streamingContext,[ZK quorum], [consumer group id], [per-topic number of Kafka partitions to consume]);
  ```

- 需要注意的要点

  1. Kafka中的topic的partition，与Spark中的RDD的partition是没有关系的。所以，在KafkaUtils.createStream()中，提高partition的数量，只会增加一个Receiver中，读取partition的线程的数量。不会增加Spark处理数据的并行度。
  2. 可以创建多个Kafka输入DStream，使用不同的consumer group和topic，来通过多个receiver并行接收数据。
  3. 如果基于容错的文件系统，比如HDFS，启用了预写日志机制，接收到的数据都会被复制一份到预写日志中。因此，在KafkaUtils.createStream()中，设置的持久化级别是StorageLevel.MEMORY_AND_DISK_SER。

### Kafka命令

```sh
bin/kafka-topics.sh --zookeeper 192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181 --topic WordCount --replication-factor 1 --partitions 1 --create

bin/kafka-console-producer.sh --broker-list 192.168.75.111:9092,192.168.75.112:9092,192.168.75.113:9092 --topic WordCount

192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181

```

[Java版本](src/java/KafkaWordCount.java)

## 输入DStream之Kafka数据源实战（基于Direct的方式）

### 基于Direct的方式

- 这种新的不基于Receiver的直接方式，是在Spark 1.3中引入的，从而能够确保更加健壮的机制。替代掉使用Receiver来接收数据后，这种方式会周期性地查询Kafka，来获得每个topic+partition的最新的offset，从而定义每个batch的offset的范围。当处理数据的job启动时，就会使用Kafka的简单consumer api来获取Kafka指定offset范围的数据。

- 这种方式有如下优点：

  1. 简化并行读取：如果要读取多个partition，不需要创建多个输入DStream然后对它们进行union操作。Spark会创建跟Kafka partition一样多的RDD partition，并且会并行从Kafka中读取数据。所以在Kafka partition和RDD partition之间，有一个一对一的映射关系。

  2. 高性能：如果要保证零数据丢失，在基于receiver的方式中，需要开启WAL机制。这种方式其实效率低下，因为数据实际上被复制了两份，Kafka自己本身就有高可靠的机制，会对数据复制一份，而这里又会复制一份到WAL中。而基于direct的方式，不依赖Receiver，不需要开启WAL机制，只要Kafka中作了数据的复制，那么就可以通过Kafka的副本进行恢复

  3. 一次且仅一次的事务机制：

     基于receiver的方式，是使用Kafka的高阶API来在ZooKeeper中保存消费过的offset的。这是消费Kafka数据的传统方式。这种方式配合着WAL机制可以保证数据零丢失的高可靠性，但是却无法保证数据被处理一次且仅一次，可能会处理两次。因为Spark和ZooKeeper之间可能是不同步的。

     基于direct的方式，使用kafka的简单api，Spark Streaming自己就负责追踪消费的offset，并保存在checkpoint中。Spark自己一定是同步的，因此可以保证数据是消费一次且仅消费一次。

     ```scala
     JavaPairReceiverInputDStream<String, String> directKafkaStream = 
     KafkaUtils.createDirectStream(streamingContext,
     [key class], [value class], [key decoder class], [value decoder class],[map of Kafka parameters], [set of topics to consume]);
     ```

  ### Kafka命令

   ```sh
  bin/kafka-topics.sh --zookeeper 192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181 --topic WordCount --replication-factor 1 --partitions 1 --create
  
  bin/kafka-console-producer.sh --broker-list 192.168.75.111:9092,192.168.75.112:9092,192.168.75.113:9092 --topic WordCount
  
  192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181
  
  metadata.broker.list
  
   ```

  [Java版本](src/java/KafkaDirectWordCount.java)

