# standalone部署细节以及相关参数

### 配置集群中的worker节点

- 如果想将某台机器部署成standalone集群架构中的worker节点（会运行worker daemon进程）那么你就必须在那台机器上部署spark安装包

**配置conf/slaves文件**

- 在conf/salves文件中，哪些机器是作为worker节点的，可以配置你要在哪些机器上启动worker进程
- 默认情况下，没有conf/slaves这个文件，只有一个conf/slaves.template，而且还是空的，此时，就只是在当前主节点上启动一个master进程和一个worker进程，此时就是master进程和worker进程在一个节点上，也就是伪分布式部署
- 如果要用分布式的部署方式，需要手动将slaves.template拷贝为一份slaves文件，编辑conf/slaves文件中的内容，在conf/slaves文件中，你可以编辑要作为worker节点的机器，比如说hostname，或者ip地址，一个机器是一行，配置以后，所有的节点上，spark部署安装包中，都得去拷贝一份这个conf/slaves文件

**conf/spark-env.sh**

- spark-env.sh文件，就类似于hadoop中的core-site.xml、hdfs-site.xml等等，应该说是spark中最核心的一份配置文件，这份文件，可以对整个spark的集群部署，各个master和worker进程的相应的行为，进行充分和细节化的配置

- SPARK_MASTER_IP					指定master进程所在的机器的ip地址

- SPARK_MASTER_PORT				指定master监听的端口号（默认是7077）

- SPARK_MASTER_WEBUI_PORT	        指定master web ui的端口号（默认是8080）

- SPARK_MASTER_OPTS	                        置master的额外参数，使用"-Dx=y"设置各个参数

  > 比如说`export SPARK_MASTER_OPTS="-Dspark.deploy.defaultCores=1"`

  |参数名											|默认值						|含义|
  |:----|:---|:----|
  |spark.deploy.retainedApplications	|			200							|在spark web ui上最多显示多少个application的信息|
  |spark.deploy.retainedDrivers			|		200							|在spark web ui上最多显示多少个driver的信息|
  |spark.deploy.spreadOut							|true						|资源调度策略，spreadOut会尽量将application的executor进程分布在更多worker上，适合基于hdfs文件计算的情况，提升数据本地化概率；非spreadOut会尽量将executor分配到一个worker上，适合计算密集型的作业|
  |spark.deploy.defaultCores						|无限大						|每个spark作业最多在standalone集群中使用多少个cpu core，默认是无限大，有多少用多少|
  | spark.deploy.timeout							|60						|单位秒，一个worker多少时间没有响应之后，master认为worker挂掉了|

- SPARK_LOCAL_DIRS				         spark的工作目录，包括了shuffle map输出文件，以及持久化到磁盘的RDD等
- SPARK_WORKER_PORT				  worker节点的端口号，默认是随机的
- SPARK_WORKER_WEBUI_PORT		  worker节点的web ui端口号，默认是8081
- SPARK_WORKER_CORES				 worker节点上，允许spark作业使用的最大cpu数量，默认是机器上所有的cpu core
- SPARK_WORKER_MEMORY			 worker节点上，允许spark作业使用的最大内存量，格式为1000m，2g等，默认最小是1g内存

> 有些master和worker的配置，可以在spark-env.sh中部署时即配置，但是也可以在start-slave.sh脚本启动进程时命令行参数设置，但是命令行参数的优先级比较高，会覆盖掉spark-env.sh中的配置

- SPARK_WORKER_INSTANCES		  当前机器上的worker进程数量，默认是1，可以设置成多个，但是这时一定要设置SPARK_WORKER_CORES，限制每个worker的cpu数量

- SPARK_WORKER_DIR				  spark作业的工作目录，包括了作业的日志等，默认是spark_home/work

- SPARK_WORKER_OPTS				   worker的额外参数，使用"-Dx=y"设置各个参数

  | 参数名                          | 默认值        | 含义                                                         |
  | :------------------------------ | :------------ | :----------------------------------------------------------- |
  | spark.worker.cleanup.enabled    | false         | 是否启动自动清理worker工作目录，默认是false                  |
  | spark.worker.cleanup.interval   | 1800          | 单位秒，自动清理的时间间隔，默认是30分钟                     |
  | spark.worker.cleanup.appDataTtl | 7 * 24 * 3600 | 默认将一个spark作业的文件在worker工作目录保留多少时间，默认是7天 |

- SPARK_DAEMON_MEMORY			  分配给master和worker进程自己本身的内存，默认是1g
- SPARK_DAEMON_JAVA_OPTS			设置master和worker自己的jvm参数，使用"-Dx=y"设置各个参数
- SPARK_PUBLISC_DNS				  master和worker的公共dns域名，默认是没有的

**spark所有的启动和关闭shell脚本**
|脚本|功能|
|:----|:---|
|sbin/start-all.sh				|根据配置，在集群中各个节点上，启动一个master进程和多个worker进程|
|sbin/stop-all.sh				|在集群中停止所有master和worker进程|
|sbin/start-master.sh			|在本地启动一个master进程|
|sbin/stop-master.sh				|关闭master进程|
|sbin/start-slaves.sh			|根据conf/slaves文件中配置的worker节点，启动所有的worker进程|
|sbin/stop-slaves.sh				|关闭所有worker进程|
|sbin/start-slave.sh				|在本地启动一个worker进程|

