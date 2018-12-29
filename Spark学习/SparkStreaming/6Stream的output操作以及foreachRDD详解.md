# Stream的output操作以及foreachRDD详解

## output操作

| Output                           | Meaning                                                      |
| -------------------------------- | ------------------------------------------------------------ |
| print                            | 打印每个batch中的前10个元素，主要用于测试，或者是不需要执行什么output操作时，用于简单触发一下job。 |
| saveAsTextFile(prefix, [suffix]) | 将每个batch的数据保存到文件中。每个batch的文件的命名格式为：prefix-TIME_IN_MS[.suffix] |
| saveAsObjectFile                 | 同上，但是将每个batch的数据以序列化对象的方式，保存到SequenceFile中。 |
| saveAsHadoopFile                 | 同上，将数据保存到Hadoop文件中                               |
| foreachRDD                       | 最常用的output操作，遍历DStream中的每个产生的RDD，进行处理。可以将每个RDD中的数据写入外部存储，比如文件、数据库、缓存等。通常在其中，是针对RDD执行action操作的，比如foreach。 |

- DStream中的所有计算，都是由output操作触发的，比如print()。如果没有任何output操作，那么，压根儿就不会执行定义的计算逻辑。
- 此外，即使你使用了foreachRDD output操作，也必须在里面对RDD执行action操作，才能触发对每一个batch的计算逻辑。否则，光有foreachRDD output操作，在里面没有对RDD执行action操作，也不会触发任何逻辑。

## foreachRDD详解

- 通常在foreachRDD中，都会创建一个Connection，比如JDBC Connection，然后通过Connection将数据写入外部存储。

  **误区一**：在RDD的foreach操作外部，创建Connection

  这种方式是错误的，因为它会导致Connection对象被序列化后传输到每个Task中。而这种Connection对象，实际上一般是不支持序列化的，也就无法被传输。

  ```scala
  dstream.foreachRDD { rdd =>
    val connection = createNewConnection() 
    rdd.foreach { record => connection.send(record)
    }
  }
  ```

  **误区二**：在RDD的foreach操作内部，创建Connection

  这种方式是可以的，但是效率低下。因为它会导致对于RDD中的每一条数据，都创建一个Connection对象。而通常来说，Connection的创建，是很消耗性能的。

  ```scala
  dstream.foreachRDD { rdd =>
    rdd.foreach { record =>
      val connection = createNewConnection()
      connection.send(record)
      connection.close()
    }
  }
  ```

- **合理方式一** ：使用RDD的foreachPartition操作，并且在该操作内部，创建Connection对象，这样就相当于是，为RDD的每个partition创建一个Connection对象，节省资源的多了。

  ```scala
  dstream.foreachRDD { rdd =>
    rdd.foreachPartition { partitionOfRecords =>
      val connection = createNewConnection()
      partitionOfRecords.foreach(record => connection.send(record))
      connection.close()
    }
  }
  ```

- **合理方式二**：自己手动封装一个静态连接池，使用RDD的foreachPartition操作，并且在该操作内部，从静态连接池中，通过静态方法，获取到一个连接，使用之后再还回去。这样的话，甚至在多个RDD的partition之间，也可以复用连接了。而且可以让连接池采取懒创建的策略，并且空闲一段时间后，将其释放掉。

  ```scala
  dstream.foreachRDD { rdd =>
    rdd.foreachPartition { partitionOfRecords =>
      val connection = ConnectionPool.getConnection()
      partitionOfRecords.foreach(record => connection.send(record))
      ConnectionPool.returnConnection(connection)  
    }
  }
  ```

  案例：改写UpdateStateByKeyWordCount，将每次统计出来的全局的单词计数，写入一份，到MySQL数据库中。

  [Java版本实例](src/java/PersistWordCount.java)

- 建表语句

  ```sql
  create table wordcount (
    id integer auto_increment primary key,
    updated_time timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    word varchar(255),
    count integer
  );
  ```

## 与Spark SQL结合使用

- Spark Streaming最强大的地方在于，可以与Spark Core、Spark SQL整合使用，之前已经通过transform、foreachRDD等算子看到，如何将DStream中的RDD使用Spark Core执行批处理操作。现在就来看看，如何将DStream中的RDD与Spark SQL结合起来使用。

  案例：每隔10秒，统计最近60秒的，每个种类的每个商品的点击次数，然后统计出每个种类top3热门的商品。

  [Java版本实例](src/java/Top3HotProduct.java)

  [Scala版本实例](src/scala/Top3HotProduct.scala)