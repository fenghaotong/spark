# yarn模式原理讲解

### yarn-client原理

- driver在本机启动
- driver跟cluster manager申请资源，yarn-client，ResourceManager
- RM分配一个container资源，在一个NM上，启动AM（ExecutorLauncher）
- AM会找RM去申请资源，启动executor进程
- RM会分配container给AM，然后AM会拿着container去找对应的NM，去启动executor进程
- driver就可以给executor进程分配task，执行作业

![](img\yarn-client模式原理.png)



### yarn-cluster原理

**yarn-cluster与yarn-client唯一的不同**

- yarn-client下，driver运行在spark-submit提交的机器上，ApplicationMaster只是相当于一个ExecutorLauncher，仅仅负责申请启动executor；具体负责调度的，还是driver
- yarn-cluster下，ApplicationMaster是driver；具体负责调度的，也是ApplicationMaster
- yarn-client下，driver所在机器可能跟yarn集群不在一起，所以性能可能比较差
- yarn-cluster下，driver（AM）就在yarn集群中，进行复杂调度的时候，网络通信性能较好
- yarn-client用于在测试环境中，进行测试，方便你看日志
- yarn-cluster推荐作业部署上线运行时使用，性能比较好

![](img\yarn-cluster原理.png)

### yarn模式运行spark作业

**前提**

- 如果想要让spark作业可以运行在yarn上面，那么首先就必须在spark-env.sh文件中，配置HADOOP_CONF_DIR或者YARN_CONF_DIR属性，值为hadoop的配置文件目录，即HADOOP_HOME/etc/hadoop，其中包含了hadoop和yarn所有的配置文件，比如hdfs-site、yarn-site等，spark需要这些配置来读写hdfs，以及连接到yarn resourcemanager上，这个目录中包含的配置文件都会被分发到yarn集群中去的

**在yarn模式下，也有两种运行模式**

- yarn-client模式下，driver进程会运行在提交作业的机器上，ApplicationMaster仅仅只是负责为作业向yarn申请资源（executor）而已，driver还是会负责作业调度
- yarn-cluster模式下，driver进程会运行在yarn集群的某个工作节点上，作为一个ApplicationMaster进程运行
- 跟spark standalone模式不同，通常不需要使用--master指定master URL
- cluster manager，也就是yarn resourcemanager的地址，会自动从hadoop配置目录中的配置文件中后去
- 设置--master时，指定为yarn-client或yarn-cluster即可，也就代表了上面说的两种deploy mode了
- 与standalone模式类似，yarn-client模式通常建议在测试时使用，方便你直接在提交作业的机器上查看日志
- 但是作业实际部署到生产环境进行运行的时候，还是使用yarn-cluster模式

**使用yarn-cluster模式提交时，使用以下语法即可: **

```sh
./bin/spark-submit \
--class path.to.your.Class \
--master yarn-cluster \
[options] \
<app jar> \
[app options]
```

```sh
$ ./bin/spark-submit --class org.leo.spark.study.WordCount \
    --master yarn-cluster \
    --num-executors 1 \
    --driver-memory 100m \
    --executor-memory 100m \
    --executor-cores 1 \
    --queue hadoop队列 \
    /usr/local/spark-study/spark-study.jar \
```

> --queue，在大公司里面，队列很重要
>
> 不同的数据部门，或者是不同的大数据项目，共用同一个yarn集群，运行spark作业
> 推荐一定要用--queue，指定不同的hadoop队列，做项目或部门之间的队列隔离

- 在yarn-cluster模式下运行时，首先在本地机器会启动一个YARN client进程，YARN client进程会连接到resourcemanager上，然后启动一个spark的ApplicationMaster进程
- 接着我们自己写的main类，会作为一个ApplicationMaster进程的子线程来运行，提交作业的本地机器上，YARN client进程会周期性地跟ApplicationMaster进程，拉取作业运行的进度，并打印在控制台上，一旦我们的作业完成之后，YARN client进程也就会退出了
- 使用yarn-client模式提交时，使用以下语法即可:` ./bin/spark-shell --master yarn-client`



**实验中要观察的几个点**

1. 日志
   - 命令行日志: 会详细打印你的所有的日志
   - web ui看日志: stdout、stderr

2. web ui，就不是spark://192.168.75.101:8080这种URL了，因为那是standalone集群的监控web ui
   - yarn的web ui上，才可以看到，stdout、stderr
   - http://192.168.75.101:8088/，URL，YARN web ui，来做作业的监控
   - http://driver:4040，通过yarn，进入spark application web ui
3. 进程
   - driver是什么进程
   - ApplicationMaster进程
   - executor进程

**yarn模式下**

- 我们的工程jar，是要拷贝到hdfs上面去的
- 而且它的replication，副本数量，默认是跟hadoop中的副本数量一样的
- hdfs，一个datanode，没有办法做replication，所以也没有退出safemode