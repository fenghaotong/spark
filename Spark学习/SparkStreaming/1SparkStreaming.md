# SparkStreaming大数据实时计算

## 大数据实时计算介绍

- Spark Streaming，其实就是一种Spark提供的，对于大数据，进行实时计算的一种框架。它的底层，其实，也是基于我们之前讲解的Spark Core的。基本的计算模型，还是基于内存的大数据实时计算模型。而且，它的底层的组件或者叫做概念，其实还是最核心的RDD。
- 只不多，针对实时计算的特点，在RDD之上，进行了一层封装，叫做DStream。其实，学过了Spark SQL之后，你理解这种封装就容易了。之前学习Spark SQL是不是也是发现，它针对数据查询这种应用，提供了一种基于RDD之上的全新概念，DataFrame，但是，其底层还是基于RDD的。所以，RDD是整个Spark技术生态中的核心。要学好Spark在交互式查询、实时计算上的应用技术和框架，首先必须学好Spark核心编程，也就是Spark Core。

![](img\大数据实时计算原理.png)

## SparkStreaming基本工作原理

![](img\Spark Streaming基本工作原理.png)

- Spark Streaming是Spark Core API的一种扩展，它可以用于进行**大规模、高吞吐量、容错**的实时数据流的处理。它支持从很多种数据源中读取数据，比如Kafka、Flume、Twitter、ZeroMQ、Kinesis或者是TCP Socket。并且能够使用类似高阶函数的复杂算法来进行数据处理，比如map、reduce、join和window。处理后的数据可以被保存到文件系统、数据库、Dashboard等存储中。

![](img\流程.png)

- Spark Streaming内部的基本工作原理如下：接收实时输入数据流，然后将数据拆分成多个batch，比如每收集1秒的数据封装为一个batch，然后将每个batch交给Spark的计算引擎进行处理，最后会生产出一个结果数据流，其中的数据，也是由一个一个的batch所组成的。

![](img\流程2.png)

- Spark Streaming提供了一种高级的抽象，叫做DStream，英文全称为Discretized Stream，中文翻译为“离散流”，它代表了一个持续不断的数据流。DStream可以通过输入数据源来创建，比如Kafka、Flume和Kinesis；也可以通过对其他DStream应用高阶函数来创建，比如map、reduce、join、window。

- DStream的内部，其实一系列持续不断产生的RDD。RDD是Spark Core的核心抽象，即，不可变的，分布式的数据集。DStream中的每个RDD都包含了一个时间段内的数据。

  ![](img\流程3.png)

- 对DStream应用的算子，比如map，其实在底层会被翻译为对DStream中每个RDD的操作。比如对一个DStream执行一个map操作，会产生一个新的DStream。但是，在底层，其实其原理为，对输入DStream中每个时间段的RDD，都应用一遍map操作，然后生成的新的RDD，即作为新的DStream中的那个时间段的一个RDD。底层的RDD的transformation操作，其实，还是由Spark
  Core的计算引擎来实现的。Spark Streaming对Spark Core进行了一层封装，隐藏了细节，然后对开发人员提供了方便易用的高层次的API。

![](img\流程4.png)

## 与Storm的对比分析

### 对比

| 对比点          | Storm                            | Spark Streaming                                           |
| :---------------- | :-------------------------------- | :--------------------------------------------------------- |
| 实时计算模型    | 纯实时，来一条数据，处理一条数据 | 准实时，对一个时间段内的数据收集起来，作为一个RDD，再处理 |
| 实时计算延迟度  | **毫秒级**                       | 秒级                                                      |
| 吞吐量          | 低                               | **高**                                                    |
| 事务机制        | 支持完善                         | 支持，但不够完善                                          |
| 健壮性 / 容错性 | **ZooKeeper，Acker，非常强**     | Checkpoint，WAL，一般                                     |
| 动态调整并行度  | **支持**                         | 不支持                                                    |

### 优劣分析

- 事实上，Spark Streaming绝对谈不上比Storm优秀。这两个框架在实时计算领域中，都很优秀，只是擅长的细分场景并不相同。
- Spark Streaming仅仅在吞吐量上比Storm要优秀，而吞吐量这一点，也是历来挺Spark Streaming，贬Storm的人着重强调的。但是问题是，是不是在所有的实时计算场景下，都那么注重吞吐量？不尽然。因此，通过吞吐量说Spark Streaming强于Storm，不靠谱。
- 事实上，Storm在实时延迟度上，比Spark Streaming就好多了，前者是纯实时，后者是准实时。而且，Storm的事务机制、健壮性 / 容错性、动态调整并行度等特性，都要比Spark Streaming更加优秀。
- Spark Streaming，有一点是Storm绝对比不上的，就是：它位于Spark生态技术栈中，因此Spark Streaming可以和Spark Core、Spark SQL无缝整合，也就意味着，我们可以对实时处理出来的中间数据，立即在程序中无缝进行延迟批处理、交互式查询等操作。**这个特点大大增强了Spark Streaming的优势和功能。**

### 应用场景

- 对于Storm来说：

1. 建议在那种需要纯实时，不能忍受1秒以上延迟的场景下使用，比如实时金融系统，要求纯实时进行金融交易和分析
2. 此外，如果对于实时计算的功能中，要求可靠的事务机制和可靠性机制，即数据的处理完全精准，一条也不能多，一条也不能少，也可以考虑使用Storm
3. 如果还需要针对高峰低峰时间段，动态调整实时计算程序的并行度，以最大限度利用集群资源（通常是在小型公司，集群资源紧张的情况），也可以考虑用Storm
4. 如果一个大数据应用系统，它就是纯粹的实时计算，不需要在中间执行SQL交互式查询、复杂的transformation算子等，那么用Storm是比较好的选择

- 对于Spark Streaming来说：

1. 如果对上述适用于Storm的三点，一条都不满足的实时场景，即，不要求纯实时，不要求强大可靠的事务机制，不要求动态调整并行度，那么可以考虑使用Spark Streaming
2. 考虑使用Spark Streaming最主要的一个因素，应该是针对整个项目进行宏观的考虑，即，如果一个项目除了实时计算之外，还包括了离线批处理、交互式查询等业务功能，而且实时计算中，可能还会牵扯到高延迟批处理、交互式查询等功能，那么就应该首选Spark生态，**用Spark Core开发离线批处理，用Spark SQL开发交互式查询，用Spark Streaming开发实时计算**，三者可以无缝整合，给系统提供非常高的可扩展性。

