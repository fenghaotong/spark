# 使用Java、Scala和spark-shell开发wordcount程序

## 用Java开发wordcount程序

- 配置maven环境
- 如何进行本地测试
- 如何使用spark-submit提交到spark集群进行执行（spark-submit常用参数说明，spark-submit其实就类似于hadoop的hadoop jar命令）

### 使用Java开发为例

- 创建maven项目
- 修改`pom.xml`文件，[pom.xml](src/pom.md)
- 添加一个包，创建一个`WordCountLocal`类， [WordCountLocal.java](../src/java/WordCountLocal.java)
- 在集群上运行，创建一个`WordCountCluster`类，[WordCountCluster.java](../src/java/WordCountCluster.java)
- 然后用maven打包成`jar`包，`Run As-> Run configurations->Maven Build->new`

![](img\maven.png)

- 如果打包出现错误，参考[本篇文章](https://www.cnblogs.com/shenrong/p/7129210.html)
- 将打包后的`jar`包，上传到spark1上面，然后执行

```sh
/usr/local/spark/bin/spark-submit \
--class cn.spark.study.core.WordCountCluster \
## --master spark://spark1:7077,spark2:7077 \ 提交到集群运行
--num-executors 3 \
--driver-memory 100m \
--executor-memory 100m \
--executor-cores 3 \
/usr/local/spark-study/java/spark-study-java-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
```

## 用Scala开发wordcount程序

- 下载scala ide for eclipse
- 在Java Build Path中，添加spark依赖包（如果与scala ide for eclipse原生的scala版本发生冲突，则移除原生的scala / 重新配置scala compiler）
- 创建一个`WordCount`类， [WordCount.scala](../src/scala/WordCount.scala)
- 用export导出scala spark工程
- 将打包后的`jar`包，上传到spark1上面，然后执行

```sh
/usr/local/spark/bin/spark-submit \
--class cn.spark.study.core.WordCount \
--num-executors 3 \
--driver-memory 100m \
--executor-memory 100m \
--executor-cores 3 \
/usr/local/spark-study/scala/wordcount.jar \
```

## 用spark-shell开发wordcount程序

- 常用于简单的测试

```scala
scala> val lines = sc.textFile("hdfs://spark1:9000/spark.txt")
scala> val words = lines.flatMap(line => line.split(" "))
scala> val pairs = words.map(word => (word, 1))
scala> val wordcount = pairs.reduceByKey(_ + _)
scala> wordcount.foreach(wordCount => println(wordCount._1 + " appeared " + wordCount._2 + " times."))
```

## Spark-submit补充说明

```sh
--master spark://spark1:7077,spark2:7077 \ 提交到集群运行
```

