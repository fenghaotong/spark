# Spark工程打包与spark-submit

- 我们在eclipse编写代码，基于spark api开发自己的大数据计算和处理程序
- 将我们写好的spark工程打包，比如说java开发环境中，就使用maven assembly插件来打包，将第三方依赖包都打进去jar包，生产环境中，通常是本机ssh远程连接到部署了spark客户端的linux机器上，使用scp命令将本机的jar包拷贝到远程linux机器上
- 然后在那个linux机器上，用spark-submit脚本，去将我们的spark工程，作为一次作业/application，提交到集群上去执行

**打包Spark工程**

- 要使用spark-submit提交spark应用程序，首先就必须将spark工程（java/scala）打包成一个jar包
- 如果我spark工程，依赖了其他一些第三方的组件，那就必须把所有组件jar包都打包到工程中，这样才能将完整的工程代码和第三方依赖都分发到spark集群中去
- 所以必须创建一个assembly jar来包含你所有的代码和依赖，sbt和maven都有assembly插件的（咱们课程里的java工程，就使用了maven的assembly插件）
- 配置依赖的时候（比如maven工程的pom.xml），可以把Spark和Hadoop的依赖配置成provided类型的依赖，也就是说仅仅开发和编译时有效，打包时就不将这两种依赖打到jar包里去了，因为集群管理器都会提供这些依赖
- 打好一个assembly jar包之后（也就是你的spark应用程序工程），就可以使用spark-submit脚本提交jar包中的spark应用程序了

### spark-submit

**spark-submit**

- 在spark安装目录的bin目录中，有一个spark-submit脚本，这个脚本主要就是用来提交自己开发的spark应用程序到集群上执行
- spark-submit可以通过一个统一的接口，将spark应用程序提交到所有spark支持的集群管理器上（Standalone（Master）、Yarn（ResourceManager）等），所以不需要为每种集群管理器都做特殊的配置

**--master**

1. 如果不设置，那么就是local模式
2. 如果设置spark://打头的URL，那么就是standalone模式，会提交到指定的URL的Master进程上去
3. 如果设置yarn-打头的，那么就是yarn模式，会读取hadoop配置文件，然后连接ResourceManager

**使用spark-submit提交spark应用**

```sh
# wordcount.sh

/usr/local/spark/bin/spark-submit \
--class org.leo.spark.study.WordCount \
--master spark://192.168.0.101:7077 \
--deploy-mode client \
--conf <key>=<value> \
/usr/local/spark-study/spark-study.jar \
```

以下是上面的spark-submit

- --class: spark应用程序对应的主类，也就是spark应用运行的主入口，通常是一个包含了main方法的java类或scala类，需要包含全限定包名，比如org.leo.spark.study.WordCount
- --master: spark集群管理器的master URL，standalone模式下，就是ip地址+端口号，比如spark://192.168.75.101:7077，standalone默认端口号就是7077
- --deploy-mode: 部署模式，决定了将driver进程在worker节点上启动，还是在当前本地机器上启动；默认是client模式，就是在当前本地机器上启动driver进程，如果是cluster，那么就会在worker上启动
- --conf: 配置所有spark支持的配置属性，使用key=value的格式；如果value中包含了空格，那么需要将key=value包裹的双引号中
- application-jar: 打包好的spark工程jar包，在当前机器上的全路径名
- application-arguments: 传递给主类的main方法的参数; 在shell中用${1}这种格式获取传递给shell的参数；然后在比如java中，可以通过main方法的args[0]等参数

**给main类传递参数的spark-submit**

```sh
# wordcount_param.sh

/usr/local/spark/bin/spark-submit \
--class org.leo.spark.study.WordCount \
--master spark://192.168.0.101:7077 \
--deploy-mode client \
--conf <key>=<value> \
/usr/local/spark-study/spark-study.jar \
${1}
```

### spark-submit多个示例及常用参数

```sh
# 使用local本地模式，以及8个线程运行
# --class 指定要执行的main类
# --master 指定集群模式，local，本地模式，local[8]，进程中用几个线程来模拟集群的执行
./bin/spark-submit \
  --class org.leo.spark.study.WordCount \
  --master local[8] \
  /usr/local/spark-study.jar \

# 使用standalone client模式运行
# executor-memory，指定每个executor的内存量，这里每个executor内存是2G
# total-executor-cores，指定所有executor的总cpu core数量，这里所有executor的总cpu core数量是100个
./bin/spark-submit \
  --class org.leo.spark.study.WordCount \
  --master spark://192.168.0.101:7077 \
  --executor-memory 2G \
  --total-executor-cores 100 \
  /usr/local/spark-study.jar \

# 使用standalone cluster模式运行
# supervise参数，指定了spark监控driver节点，如果driver挂掉，自动重启driver
./bin/spark-submit \
  --class org.leo.spark.study.WordCount \
  --master spark://192.168.0.101:7077 \
  --deploy-mode cluster \
  --supervise \
  --executor-memory 2G \
  --total-executor-cores 100 \
  /usr/local/spark-study.jar \

# 使用yarn-cluster模式运行
# num-executors，指定总共使用多少个executor运行spark应用
./bin/spark-submit \
  --class org.leo.spark.study.WordCount \
  --master yarn-cluster \  
  --executor-memory 20G \
  --num-executors 50 \
  /usr/local/spark-study.jar \

# 使用standalone client模式，运行一个python应用
./bin/spark-submit \
  --master spark://192.168.0.101:7077 \
  /usr/local/python-spark-wordcount.py \

--class
application jar
--master
--num-executors
--executor-memory
--total-executor-cores
--supervise
--executor-cores 
--driver-memory 

./bin/spark-submit \
  --class org.leo.spark.study.WordCount \
  --master yarn-cluster \
  --num-executors 100 \
  --executor-cores 2 \
  --executor-memory 6G \
  --driver-memory  1G \
  /usr/local/spark-study.jar \
```

