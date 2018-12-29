# 用户访问session分析-troubleshooting

### 控制shuffle reduce端缓冲大小以避免OOM

```text
spark.reducer.maxSizeInFlight 24
```

map端的task是不断的输出数据的，数据量可能是很大的。但是，其实reduce端的task，并不是等到map端task将属于自己的那份数据全部写入磁盘文件之后，再去拉取的。map端写一点数据，reduce端task就会拉取一小部分数据，立即进行后面的聚合、算子函数的应用。

每次reduce能够拉取多少数据，就由buffer来决定。因为拉取过来的数据，都是先放在buffer中的。然后才用后面的executor分配的堆内存占比（0.2），hashmap，去进行后续的聚合、函数的执行。

**reduce端缓冲（buffer），可能会出什么问题？**

- 可能是会出现，默认是48MB，也许大多数时候，reduce端task一边拉取一边计算，不一定一直都会拉满48M的数据。可能大多数时候，拉取个10M数据，就计算掉了。
- 大多数时候，也许不会出现什么问题。但是有的时候，map端的数据量特别大，然后写出的速度特别快。reduce端所有task，拉取的时候，全部达到自己的缓冲的最大极限值，缓冲，48M，全部填满。
- 这个时候，再加上你的reduce端执行的聚合函数的代码，可能会创建大量的对象。也许，一下子，内存就撑不住了，就会OOM。reduce端的内存中，就会发生内存溢出的问题。

**解决**

- 这个时候，就应该减少reduce端task缓冲的大小。宁愿多拉取几次，但是每次同时能够拉取到reduce端每个task的数量，比较少，就不容易发生OOM内存溢出的问题。（比如，可以调节成12M）
- 在实际生产环境中，我们都是碰到过这种问题的。这是典型的以性能换执行的原理。reduce端缓冲小了，不容易OOM了，但是，性能一定是有所下降的，你要拉取的次数就多了。就走更多的网络传输开销。
- 这种时候，只能采取牺牲性能的方式了，spark作业，首先，第一要义，就是一定要让它可以跑起来。分享一个经验，曾经写过一个特别复杂的spark作业，写完代码以后，半个月之内，就是跑不起来，里面各种各样的问题，需要进行troubleshooting。调节了十几个参数，其中就包括这个reduce端缓冲的大小。总算作业可以跑起来了。然后才去考虑性能的调优。

**reduce性能调优**

- Map端输出的数据量也不是特别大，然后你的整个application的资源也特别充足。200个executor、5个cpu core、10G内存。
- 其实可以尝试去增加这个reduce端缓冲大小的，比如从48M，变成96M。那么这样的话，每次reduce task能够拉取的数据量就很大。需要拉取的次数也就变少了。比如原先需要拉取100次，现在只要拉取50次就可以执行完了。
- 对网络传输性能开销的减少，以及reduce端聚合操作执行的次数的减少，都是有帮助的。最终达到的效果，就应该是性能上的一定程度上的提升。

### 解决JVM GC导致的shuffle文件拉取失败

**问题**

- 有时会出现的一种情况，非常普遍，在spark的作业中；shuffle file not found。（spark作业中，非常非常常见的）而且，有的时候，它是偶尔才会出现的一种情况。有的时候，出现这种情况以后，会重新去提交stage、task。重新执行一遍，发现就好了。没有这种错误了。log怎么看？用client模式去提交你的spark作业。比如standalone client；yarn client。一提交作业，直接可以在本地看到刷刷刷更新的log。
- 如果，executor的JVM进程，可能内存不是很够用了。那么此时可能就会执行GC。minor GC or full GC。总之一旦发生了JVM之后，就会导致executor内，所有的工作线程全部停止，比如BlockManager，基于netty的网络通信。
- 下一个stage的executor，可能是还没有停止掉的，task想要去上一个stage的task所在的exeuctor，去拉取属于自己的数据，结果由于对方正在gc，就导致拉取了半天没有拉取到。就很可能会报出，shuffle file not found。但是，可能下一个stage又重新提交了stage或task以后，再执行就没有问题了，因为可能第二次就没有碰到JVM在gc了。

**解决**

```java
spark.shuffle.io.maxRetries 3
```

- 第一个参数，意思就是说，shuffle文件拉取的时候，如果没有拉取到（拉取失败），最多或重试几次（会重新拉取几次文件），默认是3次。

```text
spark.shuffle.io.retryWait 5s
```

- 第二个参数，意思就是说，每一次重试拉取文件的时间间隔，默认是5s钟。
- 默认情况下，假如说第一个stage的executor正在进行漫长的full gc。第二个stage的executor尝试去拉取文件，结果没有拉取到，默认情况下，会反复重试拉取3次，每次间隔是五秒钟。最多只会等待3 * 5s = 15s。如果15s内，没有拉取到shuffle file。就会报出shuffle file not found。
- 针对这种情况，我们完全可以进行预备性的参数调节。增大上述两个参数的值，达到比较大的一个值，尽量保证第二个stage的task，一定能够拉取到上一个stage的输出文件。避免报shuffle file not found。然后可能会重新提交stage和task去执行。那样反而对性能也不好。

```text
spark.shuffle.io.maxRetries 60
spark.shuffle.io.retryWait 60s
```

- 最多可以忍受1个小时没有拉取到shuffle file。只是去设置一个最大的可能的值。full gc不可能1个小时都没结束吧。
- 这样就可以尽量避免因为gc导致的shuffle file not found，无法拉取到的问题。

### 解决YARN队列资源不足导致的application直接失败

**问题**

- 如果说，你是基于yarn来提交spark。比如yarn-cluster或者yarn-client。你可以指定提交到某个hadoop队列上的。每个队列都是可以有自己的资源的。
- 一个生产环境中的，给spark用的yarn资源队列的情况：500G内存，200个cpu core。比如说，某个spark application，在spark-submit里面你自己配了，executor，80个；每个executor，4G内存；每个executor，2个cpu core。你的spark作业每次运行，大概要消耗掉320G内存，以及160个cpu core。
- 乍看起来，咱们的队列资源，是足够的，500G内存，200个cpu core。
- 首先，第一点，你的spark作业实际运行起来以后，耗费掉的资源量，可能是比你在spark-submit里面配置的，以及你预期的，是要大一些的。400G内存，190个cpu core。
- 那么这个时候，的确，咱们的队列资源还是有一些剩余的。但是问题是，如果你同时又提交了一个spark作业上去，一模一样的。那就可能会出问题。
- 第二个spark作业，又要申请320G内存+160个cpu core。结果，发现队列资源不足。此时，可能会出现两种情况：（备注，具体出现哪种情况，跟你的YARN、Hadoop的版本，你们公司的一些运维参数，以及配置、硬件、资源肯能都有关系）
  1. YARN，发现资源不足时，你的spark作业，并没有hang在那里，等待资源的分配，而是直接打印一行fail的log，直接就fail掉了。
  2. YARN，发现资源不足，你的spark作业，就hang在那里。一直等待之前的spark作业执行完，等待有资源分配给自己来执行。

**方案**

- J2EE（这个项目里面，spark作业的运行，之前说过了，J2EE平台触发的，执行spark-submit脚本），限制，同时只能提交一个spark作业到yarn上去执行，确保一个spark作业的资源肯定是有的。
- 应该采用一些简单的调度区分的方式，比如说，你有的spark作业可能是要长时间运行的，比如运行30分钟；有的spark作业，可能是短时间运行的，可能就运行2分钟。此时，都提交到一个队列上去，肯定不合适。很可能出现30分钟的作业卡住后面一大堆2分钟的作业。分队列，可以申请（跟你们的YARN、Hadoop运维的同学申请）。你自己给自己搞两个调度队列。每个队列的根据你要执行的作业的情况来设置。在你的J2EE程序里面，要判断，如果是长时间运行的作业，就干脆都提交到某一个固定的队列里面去把；如果是短时间运行的作业，就统一提交到另外一个队列里面去。这样，避免了长时间运行的作业，阻塞了短时间运行的作业。
- 队列里面，无论何时，只会有一个作业在里面运行。那么此时，就应该用我们之前讲过的性能调优的手段，去将每个队列能承载的最大的资源，分配给你的每一个spark作业，比如80个executor；6G的内存；3个cpu core。尽量让你的spark作业每一次运行，都达到最满的资源使用率，最快的速度，最好的性能；并行度，240个cpu core，720个task。
- 在J2EE中，通过线程池的方式（一个线程池对应一个资源队列），来实现上述我们说的方案。

```java
ExecutorService threadPool = Executors.newFixedThreadPool(1);

threadPool.submit(new Runnable() {
			
@Override
public void run() {

}
			
});
```

### 解决各种序列化导致的报错

- 用client模式去提交spark作业，观察本地打印出来的log。如果出现了类似于Serializable、Serialize等等字眼，报错的log，那么就碰到了序列化问题导致的报错。序列化报错，很好处理。

**序列化报错要注意的三个点：**

- 你的算子函数里面，如果使用到了外部的自定义类型的变量，那么此时，就要求你的自定义类型，必须是可序列化的。

```java
final Teacher teacher = new Teacher("leo");

studentsRDD.foreach(new VoidFunction() {
 
public void call(Row row) throws Exception {
  String teacherName = teacher.getName();
  ....  
}

});

public class Teacher implements Serializable {
  
}

```

- 如果要将自定义的类型，作为RDD的元素类型，那么自定义的类型也必须是可以序列化的

```java
JavaPairRDD<Integer, Teacher> teacherRDD
JavaPairRDD<Integer, Student> studentRDD
studentRDD.join(teacherRDD)

public class Teacher implements Serializable {
  
}

public class Student implements Serializable {
  
}
```

- 不能在上述两种情况下，去使用一些第三方的，不支持序列化的类型

```java
Connection conn = 

studentsRDD.foreach(new VoidFunction() {
 
public void call(Row row) throws Exception {
  conn.....
}

});
// Connection是不支持序列化的
```

### 解决算子函数返回NULL导致的问题

在算子函数中，返回null

```java
		return actionRDD.mapToPair(new PairFunction<Row, String, Row>() {

			private static final long serialVersionUID = 1L;
			
			@Override
			public Tuple2<String, Row> call(Row row) throws Exception {
				return new Tuple2<String, Row>("-999", RowFactory.createRow("-999"));  
			}
			
		});
```

在有些算子函数里面，是需要我们有一个返回值的。但是，有时候，我们可能对某些值，就是不想有什么返回值。我们如果直接返回NULL的话，那么可以不幸的告诉大家，是不行的，会报错的。Scala.Math(NULL)，异常如果碰到你的确是对于某些值，不想要有返回值的话，有一个解决的办法：

- 在返回的时候，返回一些特殊的值，不要返回null，比如“-999”
- 在通过算子获取到了一个RDD之后，可以对这个RDD执行filter操作，进行数据过滤。filter内，可以对数据进行判定，如果是-999，那么就返回false，给过滤掉就可以了。
- 之前那个算子调优里面的coalesce算子，在filter之后，可以使用coalesce算子压缩一下RDD的partition的数量，让各个partition的数据比较紧凑一些。也能提升一些性能。

### 解决yarn-client模式导致的网卡流量激增问题

![](img\yarn-client.png)

**yarn-client模式下产生的问题**

- 由于driver是启动在本地机器的，而且driver是全权负责所有的任务的调度的，也就是说要跟yarn集群上运行的多个executor进行频繁的通信（中间有task的启动消息、task的执行统计消息、task的运行状态、shuffle的输出结果）。
- 想象一下。比如executor有100个，stage有10个，task有1000个。每个stage运行的时候，都有1000个task提交到executor上面去运行，平均每个executor有10个task。接下来问题来了，driver要频繁地跟executor上运行的1000个task进行通信。通信消息特别多，通信的频率特别高。运行完一个stage，接着运行下一个stage，又是频繁的通信。
- 在整个spark运行的生命周期内，都会频繁的去进行通信和调度。所有这一切通信和调度都是从你的本地机器上发出去的，和接收到的。这是最要人命的地方。你的本地机器，很可能在30分钟内（spark作业运行的周期内），进行频繁大量的网络通信。那么此时，你的本地机器的网络通信负载是非常非常高的。会导致你的本地机器的网卡流量会激增！！！
- 本地机器的网卡流量激增，当然不是一件好事了。因为在一些大的公司里面，对每台机器的使用情况，都是有监控的。不会允许单个机器出现耗费大量网络带宽等等这种资源的情况。运维人员。可能对公司的网络，或者其他（你的机器还是一台虚拟机），对其他机器，都会有负面和恶劣的影响。

**解决办法**

- 实际上解决的方法很简单，就是心里要清楚，yarn-client模式是什么情况下，可以使用的？yarn-client模式，通常咱们就只会使用在测试环境中，你写好了某个spark作业，打了一个jar包，在某台测试机器上，用yarn-client模式去提交一下。因为测试的行为是偶尔为之的，不会长时间连续提交大量的spark作业去测试。还有一点好处，yarn-client模式提交，可以在本地机器观察到详细全面的log。通过查看log，可以去解决线上报错的故障（troubleshooting）、对性能进行观察并进行性能调优。
- 实际上线了以后，在生产环境中，都得用yarn-cluster模式，去提交你的spark作业。
- yarn-cluster模式，就跟你的本地机器引起的网卡流量激增的问题，就没有关系了。也就是说，就算有问题，也应该是yarn运维团队和基础运维团队之间的事情了。使用了yarn-cluster模式以后，就不是你的本地机器运行Driver，进行task调度了。是yarn集群中，某个节点会运行driver进程，负责task调度。

### 解决yarn-cluster模式的JVM栈内存溢出无法执行问题

**yarn-client和yarn-cluster模式的不同之处：**

- yarn-client模式，driver运行在本地机器上的；yarn-cluster模式，driver是运行在yarn集群上某个nodemanager节点上面的。
- yarn-client会导致本地机器负责spark作业的调度，所以网卡流量会激增；yarn-cluster模式就没有这个问题。
- yarn-client的driver运行在本地，通常来说本地机器跟yarn集群都不会在一个机房的，所以说性能可能不是特别好；yarn-cluster模式下，driver是跟yarn集群运行在一个机房内，性能上来说，也会好一些。

**yarn-cluster遇到的问题**

- 有的时候，运行一些包含了spark sql的spark作业，可能会碰到yarn-client模式下，可以正常提交运行；yarn-cluster模式下，可能是无法提交运行的，会报出JVM的PermGen（永久代）的内存溢出，OOM。
- yarn-client模式下，driver是运行在本地机器上的，spark使用的JVM的PermGen的配置，是本地的spark-class文件（spark客户端是默认有配置的），JVM的永久代的大小是128M，这个是没有问题的；但是呢，在yarn-cluster模式下，driver是运行在yarn集群的某个节点上的，使用的是没有经过配置的默认设置（PermGen永久代大小），82M。
- spark-sql，它的内部是要进行很复杂的SQL的语义解析、语法树的转换等等，特别复杂，在这种复杂的情况下，如果说你的sql本身特别复杂的话，很可能会比较导致性能的消耗，内存的消耗。可能对PermGen永久代的占用会比较大。
- 所以，此时，如果对永久代的占用需求，超过了82M的话，但是呢又在128M以内；就会出现如上所述的问题，yarn-client模式下，默认是128M，这个还能运行；如果在yarn-cluster模式下，默认是82M，就有问题了。会报出PermGen Out of Memory error log。

**问题解决**

spark-submit脚本中，加入以下配置即可：

```sh
--conf spark.driver.extraJavaOptions="-XX:PermSize=128M -XX:MaxPermSize=256M"
```

- 这个就设置了driver永久代的大小，默认是128M，最大是256M。那么，这样的话，就可以基本保证你的spark作业不会出现上述的yarn-cluster模式导致的永久代内存溢出的问题。

**spark sql**

- sql，有大量的or语句。比如`where keywords='' or keywords='' or keywords=''`
- 当达到or语句，有成百上千的时候，此时可能就会出现一个driver端的jvm stack overflow，JVM栈内存溢出的问题
- JVM栈内存溢出，基本上就是由于调用的方法层级过多，因为产生了大量的，非常深的，超出了JVM栈深度限制的，递归。递归方法。我们的猜测，spark sql，有大量or语句的时候，spark sql内部源码中，在解析sql，比如转换成语法树，或者进行执行计划的生成的时候，对or的处理是递归。or特别多的话，就会发生大量的递归。
- JVM Stack Memory Overflow，栈内存溢出。这种时候，建议不要搞那么复杂的spark sql语句。采用替代方案：将一条sql语句，拆解成多条sql语句来执行。每条sql语句，就只有100个or子句以内；一条一条SQL语句来执行。根据生产环境经验的测试，一条sql语句，100个or子句以内，是还可以的。通常情况下，不会报那个栈内存溢出。

### 错误的持久化方式以及checkpoint的使用

**错误的持久化使用方式：**

- usersRDD，想要对这个RDD做一个cache，希望能够在后面多次使用这个RDD的时候，不用反复重新计算RDD；可以直接使用通过各个节点上的executor的BlockManager管理的内存 / 磁盘上的数据，避免重新反复计算RDD。

```scala
usersRDD.cache()
usersRDD.count()
usersRDD.take()
```

- 上面这种方式，不要说会不会生效了，实际上是会报错的。会报什么错误呢？会报一大堆file not found的错误。

**正确的持久化使用方式：**

```scala
usersRDD
usersRDD = usersRDD.cache()
val cachedUsersRDD = usersRDD.cache()
```

- 之后再去使用usersRDD，或者cachedUsersRDD，就可以了。就不会报错了。所以说，这个是咱们的持久化的正确的使用方式。

**使用checkpoint的情况**

持久化，大多数时候，都是会正常工作的。但是就怕，有些时候，会出现意外。比如说，缓存在内存中的数据，可能莫名其妙就丢失掉了。或者说，存储在磁盘文件中的数据，莫名其妙就没了，文件被误删了。

- 出现上述情况的时候，接下来，如果要对这个RDD执行某些操作，可能会发现RDD的某个partition找不到了。所以要对消失的partition重新计算，计算完以后再缓存和使用。
- 有些时候，计算某个RDD，可能是极其耗时的。可能RDD之前有大量的父RDD。那么如果你要重新计算一个partition，可能要重新计算之前所有的父RDD对应的partition。
- 这种情况下，就可以选择对这个RDD进行checkpoint，以防万一。进行checkpoint，就是说，会将RDD的数据，持久化一份到容错的文件系统上（比如hdfs）。在对这个RDD进行计算的时候，如果发现它的缓存数据不见了。优先就是先找一下有没有checkpoint数据（到hdfs上面去找）。如果有的话，就使用checkpoint数据了。不至于说是去重新计算。
- checkpoint，其实就是可以作为是cache的一个备胎。如果cache失效了，checkpoint就可以上来使用了。checkpoint有利有弊，利在于，提高了spark作业的可靠性，一旦发生问题，还是很可靠的，不用重新计算大量的rdd；但是弊在于，进行checkpoint操作的时候，也就是将rdd数据写入hdfs中的时候，还是会消耗性能的。
- checkpoint，用性能换可靠性。

**checkpoint的原理**

- 在代码中，用SparkContext，设置一个checkpoint目录，可以是一个容错文件系统的目录，比如hdfs；
- 在代码中，对需要进行checkpoint的rdd，执行RDD.checkpoint()；
- RDDCheckpointData（spark内部的API），接管你的RDD，会标记为marked for checkpoint，准备进行checkpoint
- job运行完之后，会调用一个finalRDD.doCheckpoint()方法，会顺着rdd lineage，回溯扫描，发现有标记为待checkpoint的rdd，就会进行二次标记，inProgressCheckpoint，正在接受checkpoint操作
- job执行完之后，就会启动一个内部的新job，去将标记为inProgressCheckpoint的rdd的数据，都写入hdfs文件中。（备注，如果rdd之前cache过，会直接从缓存中获取数据，写入hdfs中；如果没有cache过，那么就会重新计算一遍这个rdd，再checkpoint）
- 将checkpoint过的rdd之前的依赖rdd，改成一个CheckpointRDD*，强制改变你的rdd的lineage。后面如果rdd的cache数据获取失败，直接会通过它的上游CheckpointRDD，去容错的文件系统，比如hdfs，中，获取checkpoint的数据。

