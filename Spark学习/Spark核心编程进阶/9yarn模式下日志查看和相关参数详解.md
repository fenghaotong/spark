# yarn模式下日志查看和相关参数详解

### yarn模式下日志查看

- 在yarn模式下，spark作业运行相关的executor和ApplicationMaster都是运行在yarn的container中的
- 一个作业运行完了以后，yarn有两种方式来处理spark作业打印出的日志

**第一种是聚合日志方式（推荐，比较常用）**

- 这种方式的话，顾名思义，就是说，将散落在集群中各个机器上的日志，最后都给聚合起来，让我们可以统一查看
- 如果打开了日志聚合的选项，即`yarn.log-aggregation-enable`，container的日志会拷贝到hdfs上去，并从机器中删除
- 对于这种情况，可以使用`yarn logs -applicationId <app ID>`命令，来查看日志
- yarn logs命令，会打印出application对应的所有container的日志出来，当然，因为日志是在hdfs上的，我们自然也可以通过hdfs的命令行来直接从hdfs中查看日志
- 日志在hdfs中的目录，可以通过查看`yarn.nodemanager.remote-app-log-dir`和`yarn.nodemanager.remote-app-log-dir-suffi`x属性来获知

**第二种 web ui**

- 日志也可以通过spark web ui来查看executor的输出日志
- 但是此时需要启动History Server，需要让spark history server和mapreduce history server运行着
- 并且在yarn-site.xml文件中，配置yarn.log.server.url属性
- spark history server web ui中的log url，会将你重定向到mapreduce history server上，去查看日志

**第三种 分散查看（通常不推荐）**

- 如果没有打开聚合日志选项，那么日志默认就是散落在各个机器上的本次磁盘目录中的，在`YARN_APP_LOGS_DIR`目录下
- 根据hadoop版本的不同，通常在`/tmp/logs`目录下，或者`$HADOOP_HOME/logs/userlogs`目录下
- 如果你要查看某个container的日志，那么就得登录到那台机器上去，然后到指定的目录下去，找到那个日志文件，然后才能查看

### yarn模式相关参数

```sh
$ ./bin/spark-submit
--conf *
```

|属性名称		|									默认值		|					含义|
|:----|:----|:-------|
|spark.yarn.am.memory					|			512m	|						client模式下，YARN Application Master使用的内存总量|
|spark.yarn.am.cores		|							1								|client模式下，Application Master使用的cpu数量|
|spark.driver.cores		|							1								|cluster模式下，driver使用的cpu core数量，driver与Application Master运行在一个进程中，所以也控制了Application Master的cpu数量|
|spark.yarn.am.waitTime		|						100s							|cluster模式下，Application Master要等待SparkContext初始化的时长; client模式下，application master等待driver来连接它的时长|
|spark.yarn.submit.file.replication		|			hdfs副本数						|作业写到hdfs上的文件的副本数量，比如工程jar，依赖jar，配置文件等，最小一定是1|
|spark.yarn.preserve.staging.files		|			false							|如果设置为true，那么在作业运行完之后，会避免工程jar等文件被删除掉|
|spark.yarn.scheduler.heartbeat.interval-ms			|3000							|application master向resourcemanager发送心跳的间隔，单位ms|
|spark.yarn.scheduler.initial-allocation.interval	|200ms							|application master在有pending住的container分配需求时，立即向resourcemanager发送心跳的间隔|
|spark.yarn.max.executor.failures		|			executor数量*2，最小3			|整个作业判定为失败之前，executor最大的失败次数|
|spark.yarn.historyServer.address		|			无	|							spark history server的地址|
|spark.yarn.dist.archives			|				无	|							每个executor都要获取并放入工作目录的archive|
|spark.yarn.dist.files				|				无	|							每个executor都要放入的工作目录的文件|
|spark.executor.instances		|					2	|							默认的executor数量|
|spark.yarn.executor.memoryOverhead			|		executor内存10%					|每个executor的堆外内存大小，用来存放诸如常量字符串等东西|
|spark.yarn.driver.memoryOverhead	|				driver内存7%				|	同上|
|spark.yarn.am.memoryOverhead		|				AM内存7%						|同上|
|spark.yarn.am.port			|						随机		|					application master端口|
|spark.yarn.jar			|							无|								spark jar文件的位置|
|spark.yarn.access.namenodes	|						无	|							spark作业能访问的hdfs namenode地址|
|spark.yarn.containerLauncherMaxThreads		|		25	|							application master能用来启动executor container的最大线程数量|
|spark.yarn.am.extraJavaOptions						|无	|							application master的jvm参数|
|spark.yarn.am.extraLibraryPath						|无								|application master的额外库路径|
|spark.yarn.maxAppAttempts		|													|提交spark作业最大的尝试次数|
|spark.yarn.submit.waitAppCompletion			|		true							|cluster模式下，client是否等到作业运行完再退出|