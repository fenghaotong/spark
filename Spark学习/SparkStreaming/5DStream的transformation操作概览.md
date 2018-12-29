# DStream的transformation操作

## 概览

| Transformation | Meaning                                                      |
| -------------- | ------------------------------------------------------------ |
| map            | 对传入的每个元素，返回一个新的元素                           |
| flatMap        | 对传入的每个元素，返回一个或多个元素                         |
| filter         | 对传入的元素返回true或false，返回的false的元素被过滤掉       |
| union          | 将两个DStream进行合并                                        |
| count          | 返回元素的个数                                               |
| reduce         | 对所有values进行聚合                                         |
| countByValue   | 对元素按照值进行分组，对每个组进行计数，最后返回<K, V>的格式 |
| reduceByKey    | 对key对应的values进行聚合                                    |
| cogroup        | 对两个DStream进行连接操作，一个key连接起来的两个RDD的数据，都会以`Iterable<V>`的形式，出现在一个Tuple中。 |
| join             | 对两个DStream进行join操作，每个连接起来的pair，作为新DStream的RDD的一个元素 |
| transform        | 对数据进行转换操作                                           |
| updateStateByKey | 为每个key维护一份state，并进行更新（**这个，我认为，是在普通的实时计算中，最有用的一种操作**） |
| window           | 对滑动窗口数据执行操作（**实时计算中最有特色的一种操作**） |

## updateStateByKey以及基于缓存的实时wordcount程序

- updateStateByKey操作，可以让我们为每个key维护一份state，并持续不断的更新该state。
  1. 首先，要定义一个state，可以是任意的数据类型；
  2. 其次，要定义state更新函数——指定一个函数如何使用之前的state和新值来更新state。
- 对于每个batch，Spark都会为每个之前已经存在的key去应用一次state更新函数，无论这个key在batch中是否有新的数据。如果state更新函数返回none，那么key对应的state就会被删除。

> 当然，对于每个新出现的key，也会执行state更新函数。
>
> 注意，updateStateByKey操作，要求必须开启Checkpoint机制。

案例：基于缓存的实时wordcount程序（**在实际业务场景中，这个是非常有用的**）

[Java版本实例](src/java/UpdateStateByKeyWordCount.java)

[Scala版本实例](src/scala/UpdateStateByKeyWordCount.scala)

## transform以及广告计费日志实时黑名单过滤案例实战

- transform操作，应用在DStream上时，可以用于执行任意的RDD到RDD的转换操作。它可以用于实现，DStream API中所没有提供的操作。比如说，DStream API中，并没有提供将一个DStream中的每个batch，与一个特定的RDD进行join的操作。但是我们自己就可以使用transform操作来实现该功能。
- DStream.join()，只能join其他DStream。在DStream每个batch的RDD计算出来之后，会去跟其他DStream的RDD进行join。

案例：广告计费日志实时黑名单过滤

[Java版本实例](src/java/TransformBlacklist.java)

[Scala版本实例](src/scala/TransformBlacklist.scala)

## window滑动窗口以及热点搜索词滑动统计案例实战

- Spark Streaming提供了滑动窗口操作的支持，从而让我们可以对一个滑动窗口内的数据执行计算操作。每次掉落在窗口内的RDD的数据，会被聚合起来执行计算操作，然后生成的RDD，会作为window DStream的一个RDD。比如下图中，就是对每三秒钟的数据执行一次滑动窗口计算，这3秒内的3个RDD会被聚合起来进行处理，然后过了两秒钟，又会对最近三秒内的数据执行滑动窗口计算。所以每个滑动窗口操作，都必须指定两个参数，窗口长度以及滑动间隔，而且这两个参数值都必须是batch间隔的整数倍。（Spark Streaming对滑动窗口的支持，是比Storm更加完善和强大的）

![](img\流程5.png)

| Transform             | 意义                                     |
| --------------------- | ---------------------------------------- |
| window                | 对每个滑动窗口的数据执行自定义的计算     |
| countByWindow         | 对每个滑动窗口的数据执行count操作        |
| reduceByWindow        | 对每个滑动窗口的数据执行reduce操作       |
| reduceByKeyAndWindow  | 对每个滑动窗口的数据执行reduceByKey操作  |
| countByValueAndWindow | 对每个滑动窗口的数据执行countByValue操作 |

案例：热点搜索词滑动统计，每隔10秒钟，统计最近60秒钟的搜索词的搜索频次，并打印出排名最靠前的3个搜索词以及出现次数

[Java版本实例](src/java/WindowHotWord.java)

[Scala版本实例](src/scala/WindowHotWord.scala)

