# standalone作业监控和日志记录

- standalone模式下的作业的监控，很简单，就是通过我们之前给大家看的spark web ui
- spark standalone模式，提供了一个web界面来让我们监控集群，并监控所有的作业的运行
  web界面上，提供了master和worker的相关信息，默认的话，我们的web界面是运行在master机器上的8080端口
- 可以通过配置spark-env.sh文件等方式，来配置web ui的端口

**Spark web ui**

1. 哪些作业在跑
2. 哪些作业跑完了，花了多少时间，使用了多少资源
3. 哪些作业失败了
4. 看每个application在每个executor上的日志
5. stdout，可以显示我们用System.out.println打印出来的日志，stderr，可以显示我们用System.err.println打印出来的日志

**application web ui**

- application detail ui，其实就是作业的driver所在的机器的4040端口，可以看到job、stage、task的详细运行信息，shuffle read、shuffle write、gc、运行时间、每个task分配的数据量，定位很多性能问题、troubleshooting等等，task数据分布不均匀，那么就是数据倾斜，哪个stage运行的时间最慢，通过之前讲解的stage划分算法，去你的代码里定位到，那个stage对应的是哪一块儿代码，那段代码为什么会运行太慢，是不是可以用我们之前讲解的一些性能优化的策略，去优化一下性能
- 但是有个问题，作业运行完了以后，就看不到了，此时跟history server有关，后面会全局的去配置整个spark集群的监控和日志

**日志记录**

1. 系统级别的，spark自己的日志记录
2. 我们在程序里面，用log4j，或者System.out.println打印出来的日志



我们自己在spark作业代码中，打出来的日志，比如用System.out.println()等，是打到每个作业在每个节点的工作目录中去的，默认是SPARK_HOME/work目录下，这个目录下，每个作业都有两个文件，一个是stdout，一个是stderr，分别代表了标准输出流和异常输出流



### 运行中作业监控以及手工打印日志

[Java](src/java/WordCount.java)

