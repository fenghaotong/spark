# Spark术语

|术语|介绍|
|:---|:-----|
|Application			|spark应用程序，说白了，就是用户基于spark api开发的程序，一定是通过一个有main方法的类执行的，比如java开发spark，就是在eclipse中，建立的一个工程|
|Application Jar		|这个就是把写好的spark工程，打包成一个jar包，其中包括了所有的第三方jar依赖包，比如java中，就用maven+assembly插件打包最方便|
|Driver Program		|说白了，就是运行程序中main方法的进程，这就是driver，也叫driver进程|
|Cluster Manager		|集群管理器，就是为每个spark application，在集群中调度和分配资源的组件，比如Spark Standalone、YARN、Mesos等|
|Deploy Mode			|部署模式，无论是基于哪种集群管理器，spark作业部署或者运行模式，都分为两种，client和cluster，client模式下driver运行在提交spark作业的机器上；cluster模式下，运行在spark集群中|
|Worker Node			|集群中的工作节点，能够运行executor进程，运行作业代码的节点|
|Executor			|集群管理器为application分配的进程，运行在worker节点上，负责执行作业的任务，并将数据保存在内存或磁盘中，每个application都有自己的executor|
|Job					|每个spark application，根据你执行了多少次action操作，就会有多少个job|
|Stage				|每个job都会划分为多个stage（阶段），每个stage都会有对应的一批task，分配到executor上去执行|
|Task				|driver发送到executor上执行的计算单元，每个task负责在一个阶段（stage），处理一小片数据，计算出对应的结果|

**deploy mode，分为两种**

1. client模式：主要用于测试
2. cluster模式：主要用于生产环境

- client模式，区别就在于driver启动的位置，你在哪台机器上提交spark application，在那台机器上，就会启动driver进程，直接会去启动一个jvm进程，开始执行你的main类
- cluster模式，spark application或者叫做spark作业，提交到cluster manager，cluster manager负责在集群中某个节点上，启动driver进程

> cluster mode：集群模式，常用的有两种，standalone和yarn
> standalone模式，集群，Master进程和Worker进程，组成的集群
> yarn模式，集群，ResourceManager进程和NodeManager进程，组成的集群

无论是standalone、yarn，都是分为这两种模式的

standalone client、standalone cluster

yarn client、yarn cluster

**worker node**

- worker node，代表的是一个工作节点，并不一定就是指代的standalone模式下的Worker进程所在的节点
- 负责启动executor进程，执行task计算任务，实际执行我们写的代码，处理一部分数据
- 节点，也就是集群中的从节点（slave节点），统一叫做worker node
- standalone模式下，基于spark的Master进程和Worker进程组成的集群，Worker进程所在节点，也就是Worker节点
- yarn模式下，yarn的nodemanager进程所在的节点，也就叫做worker node，工作节点

**Job stage task**

- job，作业，一个spark application / spark作业，可能会被分解为一个或者多个job，分解的标准，就是说你的spark代码中，用了几次action操作，就会有几个job
- stage，阶段，每个job可能会被分解为一个或者多个stage，分解的标准，你在job的代码中，执行了几次shuffle操作（reduceByKey、groupByKey、countByKey），执行一次shuffle操作，job中就会有两个stage，如果一次shuffle都没执行，那就只有一个stage
- task，任务，最小的计算单元，每个stage会对应一批task，具体的数量，是可以spark自动计算，根据底层的文件（hdfs、hive、本地文件）大小来划分，默认一个hdfs block对应一个task；也可以自己手动通过spark.default.parallelism参数来设置；每个task就处理一小片数据



![](img\Spark的核心术语.png)