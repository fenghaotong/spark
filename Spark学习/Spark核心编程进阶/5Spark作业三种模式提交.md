# Spark作业三种模式提交

### local模式提交spark作业

- spark作业运行集群，有两种部署方式，一种是Spark Standalone集群，还有一种是YARN集群+Spark客户端
- 提交spark作业的两种主要方式，就是Spark Standalone和YARN，这两种方式，分别还分为两种模式，分别是client mode和cluster mode
- 在体验standalone提交模式之前，先得体验一种Spark中最基本的一种提交模式，也就是local模式

**local模式的基本原理**

- local模式下，没有所谓的master+worker这种概念
- local模式，相当于，启动一个本地进程，然后在一个进程内，模拟spark集群中作业的运行
  一个spark作业，就对应了进程中的一个或多个executor线程，就开始执行，包括作业的调度，task分配
- 在实际工作当中，local模式，主要用于测试，最常见的就是在我们的开发环境中，比如说eclipse，或者是InteliJ IDEA，直接运行spark程序，此时就可以采用local模式
- 本次实验，通过在eclipse中使用local来运行，以及在生产机器上（linux机器）上用local模式来运行

[Java版本实验](src/java/WordCount.java)

### Standalone client模式提交spark作业

- local模式下，我们都不会放到生产机器上面去提交，其实仅仅用于eclipse中运行spark作业，以及打断点，调试spark作业来用，用local模式执行，我们都会手工生成一份数据来使用
- 部署在测试机器上去，进行测试运行spark作业的时候，都是使用client模式（standalone client模式，或standalone cluster模式），client模式下，提交作业以后，driver在本机启动，可以实时看到详细的日志信息，方便你追踪和排查错误

**standalone模式下提交spark作业**

- standalone模式提交，唯一与local区别，就是一定要想办法将master设置成`spark://master_ip:port`，比如`spark://192.168.0.103:7077`

- 三种设置master的方式

  1. 硬编码: SparkConf.setMaster("spark://IP:PORT")
  2. spark-submit: --master spark://IP:PORT (最合适)
  3. spark-shell: --master spark://IP:PORT

- 一般我们都是用spark-submit脚本来提交spark application的，很少用spark-shell，spark-shell仅仅用于实验和测试一些特别简单的spark作业

- 使用spark-submit脚本来提交时，还可以指定两种deploy mode，spark standalone cluster支持client mode和cluster mode

  1. client mode下，你在哪台机器上用spark-submit脚本提交application，driver就会在那台机器上启动
  2. cluster mode下，driver会通过master进程，随机被分配给某一个worker进程来启动

- standalone模式，是要在spark-submit中，用--master指定Master进程的URL，其次，使用standalone client模式或cluster模式，是要在spark-submit中，使用--deploy-mode client/cluster来设置，默认，如果你不设置--deploy-mode，那么就是client模式

- 使用spark-submit脚本来提交application时，application jar是会自动被分发到所有worker节点上去的，对于你的application依赖的额外jar包，可以通过spark-submit脚本中的--jars标识，来指定，可以使用逗号分隔多个jar

  > 比如说，你写spark-sql的时候，有的时候，在作业中，要往mysql中写数据，此时可能会出现找不到mysql驱动jar包的情况
  >
  > 此时，就需要你手动在spark-submit脚本中，使用--jars，加入一些依赖的jar包

**提交standalone client模式的作业**

1. --master和--deploy-mode，来提交作业
2. 观察作业执行日志，可以看到去连接spark://192.168.75.101:7077 URL的master
3. 观察一些spark web ui，可以看到completed applications一栏中，有我们刚刚运行的作业，比对一下ui上的applicationId和日志中的applicationId
4. 重新再执行一次作业，一执行，立即看一下jps查看进程，看看standalone client模式提交作业的时候，当前机器上会启动哪些进程
   - SparkSubmit: 也就是我们的driver进程，在本机上启动（spark-submit所在的机器）
   - CoarseGrainedExecutorBackend（内部持有一个Executor对象）: 在你的执行spark作业的worker机器上，肯定会给你的作业分配和启动一个executor进程，具体名字就是那个

### Standalone clustert模式提交spark作业

- 通常用于，spark作业部署到生产环境中去使用，是用standalone cluster模式，因为这种模式，会由master在集群中，某个节点上，来启动driver，然后driver会进行频繁的作业调度
  此时driver跟集群在一起，那么是性能比较高的
- standalone client模式，在spark-submit脚本执行的机器上，会启动driver进程，然后去进行整个作业的调度，通常来说，你的spark-submit脚本能够执行的机器，也就是，作为一个开发人员能够登录的机器，通常不会直接是spark集群部署的机器，因为，随便谁，都登录到spark集群中，某个机器上去，执行一个脚本，没有安全性可言了，所有此时呢，是这样子，用client模式，你的机器，可能与spark集群部署的机器，都不在一个机房，或者距离很远，那么此时通常遥远的频繁的网络通信会影响你的整个作业的执行性能 
- 此外，standalone cluster模式，还支持监控你的driver进程，并且在driver挂掉的时候，自动重启该进程，要使用这个功能，在spark-submit脚本中，使用--supervise标识即可，这个跟我们的这个作业关系不是太大，主要是用于spark streaming中的HA高可用性，配置driver的高可用
- 如果想要杀掉反复挂掉的driver进程，使用以下命令即可: `bin/spark-class org.apache.spark.deploy.Client kill <master url> <driver ID>`  如果要查看driver id，通过`http://<maser url>:8080`即可查看到

**实验观察**

1. 日志：命令行只会打印一点点内容，没有什么东西; 主要呢，是在web ui上，可以看到日志
2. web ui: running drivers出现，此时就可以看到在spark集群中执行的driver的信息; completed drivers
3. 进程: SparkSubmit一闪而过，仅仅只是将driver注册到master上，由master来启动driver，马上就停止了; 在Worker上，会启动DriverWrapper进程，如果能够申请到足够的cpu资源，其实还会在其他worker上，启动CoarseGrainedExecutorBackend进程
4. 可以通过点击running中的application，去查看作业中的每个job、每个stage、每个executor和task的执行信息，4040端口来看的
5. 对于正在运行的driver或application，可以在web ui上，点击kill按钮，给杀掉

**为分布式出现的情况**

- 首先呢，cluster模式下，driver也是要通过worker来启动的，executor也是要通过worker来启动的，
- 我们可以看到，此时driver已经启动起来了，在web ui上是可以看到的，包括driver ID，
- 通过web ui就可以看到，driver在唯一的worker上启动了，已经获取到了一个cpu core了
- driver去跟master申请资源，启动一个executor进程，但是问题来了，此时我们的worker进程，就只有一个，而且只有一个cpu core，那么，master的资源调度算法中，始终无法找到还有空闲cpu资源的worker，所以作业一直处于等待，waiting的一个状态
- 所以，我们的作业在当前1个cpu core下，是无法通过cluster模式来运行的