# SparkStandalone集群架构

### SparkStandalone集群

- 集群管理器，cluster manager：Master进程，工作节点：Worker进程
-  搭建了一套Hadoop集群（HDFS+YARN）
-   HDFS：NameNode、DataNode、SecondaryNameNode
- YARN：ResourceManager、NodeManager
-  Spark集群（Spark Standalone集群）：Master、worker

### SparkStandalone集群模式与YARN集群模式

- 如果是Spark Standalone模式，甚至你根本不需要YARN集群，甚至连HDFS集群都可以不需要
- Spark，Master+Worker集群架构，就足够了，然后就可以编写spark作业，提交作业到Master+Worker集群架构中去运行
- 一般大公司，Hadoop、Spark、Storm、Hive、MapReduce，都用到了，统一就不搭建Spark集群（Master+Worker压根儿就没有），
- YARN集群，直接部署一个spark客户端，部署一个spark安装包（解压缩+配置上hadoop的配置文件目录，spark-env.sh）就可以提交spark作业给YARN集群去运行

![](img\SparkStandalone集群架构.png)

### SparkStandalone集群单独启动master和worker脚本详解

- sbin/start-all.sh脚本一旦执行，就会直接在集群（节点，部署了spark安装包）中，启动master进程和所有worker进程，sbin/start-all.sh脚本，其实是用来便捷地快速启动整个spark standalone集群的，还可以使用另外两个脚本，单独启动master和worker进程
- 如果要单独分别，启动master和worker进程的话，那么必须得先启动master进程，然后再启动worker进程，因为worker进程启动以后，需要向master进程去注册，反过来先启动worker进程，再启动这个master进程，可能会有问题
- 为什么有的时候也需要单独启动master和worker进程呢？因为在单独启动两个进程的时候，是可以通过命令行参数，为进程配置一些独特的参数，比如说监听的端口号、web ui的端口号、使用的cpu和内存，比如你想单独给某个worker节点配置不同的cpu和内存资源的使用限制，那么就可以使用脚本单独启动这个worker进程的时候，通过命令行参数来设置

**手动启动master进程**

- 需要在某个部署了spark安装包的节点上，使用sbin/start-master.sh启动
- master启动之后，启动日志就会打印一行spark://HOST:PORT URL出来，这就是master的URL地址
- worker进程就会通过这个URL地址来连接到master进程，并且进行注册
- 另外，除了worker进程要使用这个URL意外，我们自己在编写spark代码时，也可以给SparkContext的setMaster()方法，传入这个URL地址
- 然后我们的spark作业，就会使用standalone模式连接master，并提交作业
- 此外，还可以通过http://MASTER_HOST:8080 URL来访问master集群的监控web ui，那个web ui上，也会显示master的URL地址

**手动启动worker进程**

- 需要在你希望作为worker node的节点上，在部署了spark安装包的前提下，使用`sbin/start-slave.sh <master-spark-URL>`在当前节点上启动
- 如上所述，使用sbin/start-slave.sh时，需要指定master URL
- 启动worker进程之后，再访问http://MASTER_HOST:8080，在spark集群web ui上，就可以看见新启动的worker节点，包括该节点的cpu和内存资源等信息

**此外，以下参数是可以在手动启动master和worker的时候指定的**
|参数|作用|
|:---|:----|
|-h HOST, --host HOST			|在哪台机器上启动，默认就是本机，这个很少配|
|-p PORT, --port PORT			|在机器上启动后，使用哪个端口对外提供服务，master默认是7077，worker默认是随机的，也很少配|
|--webui-port PORT				|web ui的端口，master默认是8080，worker默认是8081，也很少配|
|**-c CORES, --cores CORES**			|仅限于worker，总共能让spark作业使用多少个cpu core，默认是当前机器上所有的cpu core|
|**-m MEM, --memory MEM**			|仅限于worker，总共能让spark作业使用多少内存，是100M或者1G这样的格式，默认是1g|
|**-d DIR, --work-dir DIR**			|仅限于worker，工作目录，默认是SPARK_HOME/work目录|
|--properties-file FILE			|master和worker加载默认配置文件的地址，默认是conf/spark-defaults.conf，很少配|

> 举个例子假如物理集群可能就一套，同一台机器上面，可能要部署Storm的supervisor进程，可能还要同时部署Spark的worker进程
>
> 机器，cpu和内存，既要供storm使用，还要供spark使用
>
> 这个时候，可能你就需要限制一下worker节点能够使用的cpu和内存的数量

**实验**

1. 启动master: 日志和web ui，观察master url
2. 启动worker: 观察web ui，是否有新加入的worker节点，以及对应的信息
3. 关闭master和worker
4. 再次单独启动master和worker，给worker限定，就使用500m内存，跟之前看到的worker信息比对一下内存最大使用量