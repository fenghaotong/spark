# 使用高性能方式将实时计算结果写入MySQL中

### Spark Streaming foreachRDD的正确使用方式

**误区一：在driver上创建连接对象（比如网络连接或数据库连接）**

- 如果在driver上创建连接对象，然后在RDD的算子函数内使用连接对象，那么就意味着需要将连接对象序列化后从driver传递到worker上。而连接对象（比如Connection对象）通常来说是不支持序列化的，此时通常会报序列化的异常（serialization errors）。因此连接对象必须在worker上创建，不要在driver上创建。

```scala
dstream.foreachRDD { rdd =>
  val connection = createNewConnection()  // 在driver上执行
  rdd.foreach { record =>
    connection.send(record) // 在worker上执行
  }
}
```

误区二：为每一条记录都创建一个连接对象

```scala
dstream.foreachRDD { rdd =>
  rdd.foreach { record =>
    val connection = createNewConnection()
    connection.send(record)
    connection.close()
  }
}
```

- 通常来说，连接对象的创建和销毁都是很消耗时间的。因此频繁地创建和销毁连接对象，可能会导致降低spark作业的整体性能和吞吐量。



**正确做法一：为每个RDD分区创建一个连接对象**

```scala
dstream.foreachRDD { rdd =>
  rdd.foreachPartition { partitionOfRecords =>
    val connection = createNewConnection()
    partitionOfRecords.foreach(record => connection.send(record))
    connection.close()
  }
}
```

- 比较正确的做法是：对DStream中的RDD，调用foreachPartition，对RDD中每个分区创建一个连接对象，使用一个连接对象将一个分区内的数据都写入底层MySQL中。这样可以大大减少创建的连接对象的数量。

**正确做法二：为每个RDD分区使用一个连接池中的连接对象**

```scala
dstream.foreachRDD { rdd =>
  rdd.foreachPartition { partitionOfRecords =>
    // 静态连接池，同时连接是懒创建的
    val connection = ConnectionPool.getConnection()
    partitionOfRecords.foreach(record => connection.send(record))
    ConnectionPool.returnConnection(connection)  // 用完以后将连接返回给连接池，进行复用
  }
}
```

**对于这种实时计算程序的mysql插入，有两种pattern（模式）**

1. 比较挫：每次插入前，先查询，看看有没有数据，如果有，则执行insert语句；如果没有，则执行update语句；好处在于，每个key就对应一条记录；坏处在于，本来对一个分区的数据就是一条insert batch，现在很麻烦，还得先执行select语句，再决定是insert还是update。

j2ee系统，查询某个key的时候，就直接查询指定的key就好。

2. 稍微好一点：每次插入记录，你就插入就好，但是呢，需要在mysql库中，给每一个表，都加一个时间戳（timestamp），对于同一个key，5秒一个batch，每隔5秒中就有一个记录插入进去。相当于在mysql中维护了一个key的多个版本。

j2ee系统，查询某个key的时候，还得限定是要order by timestamp desc limit 1，查询最新时间版本的数据

通过mysql来用这种方式，不是很好，很不方便后面j2ee系统的使用，不用mysql；用hbase（timestamp的多个版本，而且它不却分insert和update，统一就是去对某个行键rowkey去做更新）