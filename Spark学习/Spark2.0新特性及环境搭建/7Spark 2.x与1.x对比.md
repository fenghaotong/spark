# Spark 2.x与1.x对比

**Spark 2.x与1.x对比**

Spark 1.x：Spark Core（RDD）、Spark SQL（SQL+Dataframe+Dataset）、Spark Streaming、Spark MLlib、Spark Graphx 

Spark 2.x：Spark Core（RDD）、Spark SQL（**ANSI-SQL+Subquery**+Dataframe/**Dataset**）、Spark Streaming、**Structured Streaming**、Spark MLlib（**Dataframe/Dataset**）、Spark Graphx、**Second Generation Tungsten Engine（Whole-stage code generation+Vectorization）**

Spark 1.x到Spark 2.x，完全是一脉相承的关系，即，Spark 2.x基本上是基于Spark 1.x进行了更多的功能和模块的扩展，以及底层性能的改良。绝对不是说，Spark 2.x彻底淘汰和替代了Spark 1.x中的组件。而且实际上，对于Spark 1.x中90%以上的东西，Spark 2.x几乎都完全保留了支持和延续，并没有做任何改变。

下面我们就对Spark 2.x中的每个组件都进行分析，这些组件的基本原理，以及其适用和不适用的场景。

### Spark 2.x各组件分析

**Spark Core（RDD）**

从Spark诞生之日开始，RDD就是Spark最主要的编程接口，重要程度类似于Hadoop中的MapReduce。RDD，简单来说，就是一个不可变的分布式数据集，被分为多个partition从而在一个集群上分布式地存储。我们可以使用RDD提供的各种transformation和action算子，对RDD执行分布式的计算操作。

Spark Core/RDD是Spark生态中，不可替代的基础API和引擎，其他所有的组件几乎都是构建在它之上。未来它不会被淘汰，只是应用场景会减少而已。



Spark 2.x中，在离线批处理计算中，编程API，除了RDD以外，还增强了Dataframe/Dataset API。那么，我们到底什么时候应该使用Spark Core/RDD来进行编程呢？实际上，RDD和Dataset最大的不同在于，RDD是底层的API和内核，Dataset实际上基于底层的引擎构建的high-level的计算引擎。

1. 如果需要对数据集进行非常底层的掌控和操作，比如说，手动管理RDD的分区，或者根据RDD的运行逻辑来结合各种参数和编程来进行较为底层的调优。因为实际上Dataframe/Dataset底层会基于whole-stage code generation技术自动生成很多代码，那么就意味着，当我们在进行线上报错的troubleshooting以及性能调优时，对Spark的掌控能力就会降低。而使用Spark Core/RDD，因为其运行完全遵循其源码，因此我们完全可以在透彻阅读Spark Core源码的基础之上，对其进行troubleshooting和底层调优。**（最重要的一点）**
2. 我们要处理的数据是非结构化的，比如说多媒体数据，或者是普通文本数据。
3. 我们想要使用过程式编程风格来处理数据，而不想使用`domain-specific language`的编程风格来处理数据。
4. 我们不关心数据的schema，即元数据。
5. 我们不需要Dataframe/Dataset底层基于的第二代tungsten引擎提供的whole-stage code generation等性能优化技术。

**Spark SQL（ANSI-SQL+Subquery）**

Spark 2.x中的Spark SQL，提供了标准化SQL的支持，以及子查询的支持，大幅度提升了Spark在SQL领域的应用场景。而且本身在大数据领域中，SQL就是一个最广泛使用的用户入口，据不完全统计以及讲师的行业经验，做大数据的公司里，90%的应用场景都是基于SQL的。最典型的例子就是Hadoop，几乎用Hadoop的公司，90%都是基于Hive进行各种大数据的统计和分析。剩下10%是实时计算、机器学习、图计算。之所以有这种现象，主要就是因为SQL简单、易学、易用、直观。无论是研发人员，还是产品经理，还是运营人员，还是其他的人，都能在几天之内入门和学会SQL的使用，然后就可以基于大数据SQL引擎（比如Hive）基于企业积累的海量数据，根据自己的需求进行各种统计和分析。

此外，据Spark官方社区所说，Spark 2.x一方面对SQL的支持做了大幅度的增强，另一方面，也通过优化了底层的计算引擎（第二代tungsten引擎，whole-stage code generation等），提升了SQL的执行性能以及稳定性。

所以在Spark 2.x中，一方面，开始鼓励大家多使用Spark SQL的SQL支持，采用Spark SQL来编写SQL进行最常见的大数据统计分析。比如可以尝试将Hive中的运行的一些SQL语句慢慢迁移到Spark SQL上来。另外一方面，也提醒大家，一般一个新的大版本，都是不太稳定的，因此Spark SQL虽然在功能、性能和稳定性上做了很多的增强，但是难免还是会有很多的坑。因此建议大家在做Hive/RDBMS（比如Oracle）到Spark SQL的迁移时，要小心谨慎，一点点迁移，同时做好踩坑的准备。

**Spark SQL（Dataframe/Dataset）**

就像RDD一样，Dataframe也代表一个不可变的分布式数据集。与RDD不同的一点是，Dataframe引入了schema的概念，支持以复杂的类型作为元素类型，同时指定schema，比如Row。因此Dataframe更像是传统关系型数据库中的表的概念。为了提升开发人员对大数据的处理能力，Dataframe除了提供schema的引入，还基于Schema提供了很多RDD所不具备的high-level API，以及一些domain-specific language（特定领域编程语言）。但是在Spark 2.0中，Dataframe和Dataset合并了，Dataframe已经不是一个单独的概念了，目前仅仅只是Dataset[Row]的一个类型别名而已，你可以理解为Dataframe就是Dataset。

![](src\DataframeDataset.png)

从Spark 2.0开始，Dataset有两种表现形式：`typed API和untyped API`。我们可以认为，Dataframe就是Dataset[Row]的别名，Row就是一个untyped类型的对象，因为Row是类似于数据库中的一行，我们只知道里面有哪些列，但是有些列即使不存在，我们也可以这对这些不存在的列进行操作。因此其被定义为untyped，就是弱类型。



而Dataset[T]本身，是一种typed类型的API，其中的Object通常都是我们自己自定义的typed类型的对象，因为对象是我们自己定义的，所以包括字段命名以及字段类型都是强类型的。目前Scala支持Dataset和Dataframe两种类型，Java仅仅支持Dataset类型，Python和R因为不具备compile-time type-safety特性，因此仅仅支持Dataframe。

- Dataset API优点

  1. **静态类型以及运行时的类型安全性**

     SQL语言具有最不严格的限制，而Dataset具有最严格的限制。SQL语言在只有在运行时才能发现一些错误，比如类型错误，但是由于Dataframe/Dataset目前都是要求类型指定的（静态类型），因此在编译时就可以发现类型错误，并提供运行时的类型安全。比如说，如果我们调用了一个不属于Dataframe的API，编译时就会报错。但是如果你使用了一个不存在的列，那么也只能到运行时才能发现了。而最严格的就是Dataset了，因为Dataset是完全基于typed API来设计的，类型都是严格而且强类型的，因此如果你使用了错误的类型，或者对不存在的列进行了操作，都能在编译时就发现。

     |                | SQL     | Dataframe    | Dataset      |
     | -------------- | ------- | ------------ | ------------ |
     | Syntax Error   | Runtime | Compile Time | Compile Time |
     | Analysis Error | Runtime | Runtime      | Compile Time |

  2. **将半结构化的数据转换为typed自定义类型**

     举例来说，如果我们现在有一份包含了学校中所有学生的信息，是以JSON字符串格式定义的，比如：`{“name”:“leo”, “age”, 19, “classNo”: 1}`。我们可以自己定义一个类型，比如`case class Student(name: String, age: Integer, classNo: Integer)`。接着我们就可以加载指定的json文件，并将其转换为typed类型的Dataset[Student]，比如`val ds = spark.read.json("students.json").as[Student]`。

     **在这里，Spark会执行三个操作：**

     1. Spark首先会读取json文件，并且自动推断其schema，然后根据schema创建一个Dataframe。
     2. 在这里，会创建一个Dataframe=Dataset[Row]，使用Row来存放你的数据，因为此时还不知道具体确切的类型。
     3. 接着将Dataframe转换为Dataset[Student]，因为此时已经知道具体的类型是Student了。

     这样，我们就可以将半结构化的数据，转换为自定义的typed结构化强类型数据集。并基于此，得到之前说的编译时和运行时的类型安全保障。

  3. **API的易用性**

     Dataframe/Dataset引入了很多的high-level API，并提供了domain-specific language风格的编程接口。这样的话，大部分的计算操作，都可以通过Dataset的high-level API来完成。通过typed类型的Dataset，我们可以轻松地执行agg、select、sum、avg、map、filter、groupBy等操作。使用domain-specific language也能够轻松地实现很多计算操作，比如类似RDD算子风格的map()、filter()等。

  4. **性能**

     除了上述的优点，Dataframe/Dataset在性能上也有很大的提升。首先，Dataframe/Dataset是构建在Spark SQL引擎之上的，它会根据你执行的操作，使用Spark SQL引擎的Catalyst来生成优化后的逻辑执行计划和物理执行计划，可以大幅度节省内存或磁盘的空间占用的开销（相对于RDD来说，Dataframe/Dataset的空间开销仅为1/3~1/4），也能提升计算的性能。其次，Spark 2.x还引入第二代Tungsten引擎，底层还会使用whole-stage code generation、vectorization等技术来优化性能。

- 什么时候应该使用Dataframe/Dataset，而不使用RDD

  1. 如果需要更加丰富的计算语义，high-level的抽象语义，以及domain-specific API。
  2. 如果计算逻辑需要high-level的expression、filter、map、aggregation、average、sum、SQL、列式存储、lambda表达式等语义，来处理半结构化，或结构化的数据。
  3. 如果需要高度的编译时以及运行时的类型安全保障。
  4. 如果想要通过Spark SQL的Catalyst和Spark 2.x的第二代Tungsten引擎来提升性能。
  5. 如果想要通过统一的API来进行离线、流式、机器学习等计算操作。
  6. 如果是R或Python的用户，那么只能使用Dataframe。



  Spark官方社区对RDD和Dataframe/Dataset的建议时，按照各自的特点，根据的需求场景，来灵活的选择最合适的引擎。甚至说，在一个Spark应用中，也可以将两者结合起来一起使用。

**Spark Streaming&Structured Streaming**

Spark Streaming是老牌的Spark流式计算引擎，底层基于RDD计算引擎。除了类似RDD风格的计算API以外，也提供了更多的流式计算语义，比如window、updateStateByKey、transform等。同时对于流式计算中重要的数据一致性、容错性等也有一定的支持。

但是Spark 2.x中也推出了全新的基于Dataframe/Dataset的Structured Streaming流式计算引擎。相较于Spark Streaming来说，其最大的不同之处在于，采用了全新的逻辑模型，提出了real-time incremental table的概念，更加统一了流式计算和离线计算的概念，减轻了用户开发的负担。同时还提供了（可能在未来提供）高度封装的特性，比如双流的全量join、与离线数据进行join的语义支持、内置的自动化容错机制、内置的自动化的一次且仅一次的强一致性语义、time-based processing、延迟数据达到的自动处理、与第三方外部存储进行整合的sink概念，等等高级特性。大幅度降低了流式计算应用的开发成本。

这里要提的一句是，首先，目前暂时建议使用Spark Streaming，因为Spark Streaming基于RDD，而且经过过个版本的考验，已经趋向于稳定。对于Structured Streaming来说，一定要强调，在Spark 2.0版本刚推出的时候，千万别在生产环境使用，因为目前官方定义为beta版，就是测试版，里面可能有很多的bug和问题，而且上述的各种功能还不完全，很多功能还没有。因此Structured Streaming的设计理念虽然非常好，但是个人建议在后续的版本中再考虑使用。目前可以保持关注和学习，并做一些实验即可。

**Spark MLlib&GraphX**

Spark MLlib未来将主要基于Dataframe/Dataset API来开发。而且还会提供更多的机器学习算法。因此可以主要考虑使用其spark.ml包下的API即可。

Spark GraphX，目前发展较为缓慢，如果有图计算相关的应用，可以考虑使用。



### Spark 2.x学习建议

纵观之前讲的内容，Spark 2.0本次，其实主要就是提升了底层的性能，搭载了第二代Tungsten引擎；同时大幅度调整和增强了ANSI-SQL支持和Dataframe/Dataset API，并将该API作为Spark未来重点发展的发现；此外，为了提供更好的流式计算解决方案，发布了一个测试版的Structured Streaming模块。

而且之前也讲解了Spark 1.x和Spark 2.x中的每一个模块。大家可以明确看到：

第一，Spark 1.x没有任何一个组件是被淘汰的；

第二，Spark这次重点改造的是Tungsten Engine、Dataframe/Dataset以及Structured Streaming，对于之前Spark 1.x课程中讲解的Spark Core、Spark SQL以及Spark Streaming，包括Spark Core的性能调优和源码剖析，集群运维管理，几乎没有做太多的调整；

第三，Spark Core、Spark SQL、Spark Streaming、Dataframe/Dataset、Structured Streaming、Spark MLlib和GraphX，每个组件目前都有其特点和用途，任何一个不是积累和过时的技术；

第五，Spark 2.0的新东西中，ANSI-SQL和Dataframe/Dataset API是可以重点尝试使用的，但是Structured Streaming还停留在实验阶段，完全不能应用到生产项目中。因此目前流式计算主要还是使用Spark Streaming。个人预计，至少要在2017年春节过后，Structured Streaming才有可能进入稳定状态，可以尝试使用。

首先，对于课程之前讲解的Spark 1.x的所有知识，目前以及之后可预见的时间范围内，都是一直有价值的，都是需要学习的。无论是Spark Core（RDD）编程，作为整个Spark生态的基石（包括Dataframe/Dataset），以及掌握Spark底层的知识；还是Spark SQL的开发，或者是Spark Streaming的开发；还有它们的性能调优、Spark Core源码剖析；以及管理运维，这些知识都没有过时，都是价值的，大家都必须认真、仔细的学习，绝对不能轻浮冒进，直接就简单学学Dataframe/Dataset，Structured Streaming，就以为自己掌握了Spark 2.x了，那是绝对错误的！

本次课程升级，主要分为三个阶段，第一个阶段就是Spark 2.x的新特性介绍，主要包括了新特性概览、发展方向、核心原理以及与1.x的对比分析、学习建议以及使用建议；第二个阶段就是Dataset的开发详解；第三个阶段就是Structured Streaming开发详解。因此在透彻掌握Spark 1.x的基础之上，再来学习Spark 2.x效果更佳。其中最重要的，是要掌握Spark第二代Tungsten引擎的性能提升原理、Spark ANSI-SQL和子查询的支持、Dataset的开发以及使用、Structured Streaming的开发以及使用。



### Spark 2.x使用建议

在透彻学习了Spark 1.x和Spark 2.x的知识体系之后，对于Spark的使用，建议如下

1. 建议开始大量尝试使用Spark SQL的标准化SQL支持以及子查询支持的特性，大部分的大数据统计分析应用，采用Spark SQL来实现。
2. 其次，对于一些无法通过SQL来实现的复杂逻辑，比如一些算法的实施，或者一些跟DB、缓存打交道的大数据计算应用，建议采用Dataframe/Dataset API来实施。
3. 接着，对于一些深刻理解课程中讲解的Spark Core/RDD，以及内核源码的高阶同学，如果遇到了因为Spark SQL和Dataframe/Dataset导致的线上的莫名其妙的报错，始终无法解决，或者是觉得有些性能，通过第二代Tungsten引擎也无法很好的调优，需要自己手工通过RDD控制底层的分区以及各种参数来进行调优，那么建议使用Spark Core/RDD来重写SQL类应用。
4. 对于流式计算应用，建议目前还是使用Spark Streaming，因为其稳定；Structured Streaming目前是beta版本，很不稳定，因此目前建议仅仅是学习和实验即可。个人预计和建议，估计至少要到2017年春节后，Structured Streaming才可能具备部署生产环境的能力。
5. 对于机器学习应用，建议使用spark.ml包下的机器学习API，因为其基于Dataframe/Dataset API实现，性能更好，而且未来是社区重点发展方向