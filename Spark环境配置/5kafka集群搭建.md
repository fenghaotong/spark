# kafka集群搭建

Kafka是一种高吞吐量的[分布式](https://baike.baidu.com/item/%E5%88%86%E5%B8%83%E5%BC%8F/19276232)发布订阅消息系统，它可以处理消费者规模的网站中的所有动作流数据。 这种动作（网页浏览，搜索和其他用户的行动）是在现代网络上的许多社会功能的一个关键因素。 这些数据通常是由于吞吐量的要求而通过处理日志和日志聚合来解决。 对于像[Hadoop](https://baike.baidu.com/item/Hadoop)的一样的[日志](https://baike.baidu.com/item/%E6%97%A5%E5%BF%97/2769135)数据和离线分析系统，但又要求实时处理的限制，这是一个可行的解决方案。Kafka的目的是通过[Hadoop](https://baike.baidu.com/item/Hadoop)的并行加载机制来统一线上和离线的消息处理，也是为了通过[集群](https://baike.baidu.com/item/%E9%9B%86%E7%BE%A4/5486962)来提供实时的消息。

## 安装scala

- 下载[scala](http://downloads.typesafe.com/scala/2.11.7/scala-2.11.7.msi)
- 将下载的`scala`包解压缩到`/usr/local`文件夹下
- 修改`scala`文件夹名字为`scala`
- 配置环境变量
- 按照上述步骤在`spark2`和`spark3`机器上都安装好`scala`。使用`scp`将`scala`和`.bashrc`拷贝到`spark2`和`spark3`上即可。

## 安装kafka

- 下载[kafka](http://archive.apache.org/dist/kafka/)
- 将下载的`kafka`包解压缩到`/usr/local`文件夹下
- 修改`kafka`文件夹名字为`kafka`
- 配置`kafka`

```sh
vi /usr/local/kafka/config/server.properties

broker.id：# 依次增长的整数，0、1、2、3、4，集群中Broker的唯一id
zookeeper.connect=192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181
```

- 安装[`slf4j`](https://www.slf4j.org/dist/)，将`slf4j`解压到`/usr/local/`目录下
- 把`slf4j`中的`slf4j-nop-*.jar`复制到`kafka`的`lib`目录下面

## 搭建kafka集群

- 按照上述步骤在spark2和spark3分别安装kafka。用scp把kafka拷贝到spark2和spark3行即可。
- 唯一区别的，就是server.properties中的broker.id，要设置为1和2

## 启动kafka集群

- 在三台机器上分别执行以下命令：

```sh
nohup bin/kafka-server-start.sh config/server.properties &
```

- 解决`kafka Unrecognized VM option 'UseCompressedOops'`问题

```sh
vi bin/kafka-run-class.sh 
if [ -z "$KAFKA_JVM_PERFORMANCE_OPTS" ]; then
  KAFKA_JVM_PERFORMANCE_OPTS="-server -XX:+UseCompressedOops -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC -Djava.awt.headless=true"
fi
# 去掉-XX:+UseCompressedOops即可
```

- 使用jps检查启动是否成功

## 测试kafka集群

- 使用基本命令检查`kafka`是否搭建成功

```sh
# 在spark1上创建一个TestTopic
bin/kafka-topics.sh --zookeeper 192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181 --topic TestTopic --replication-factor 1 --partitions 1 --create

# 在spark1上创建一个TestTopic的生产者
bin/kafka-console-producer.sh --broker-list 192.168.75.111:9092,192.168.75.112:9092,192.168.75.113:9092 --topic TestTopic

# 打开spark1的另一个session，创建一个TestTopic的消费者
bin/kafka-console-consumer.sh --zookeeper 192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181 --topic TestTopic --from-beginning

# 然后在生产者出输入
hello world

# 相应的在消费者处也会产生
hello world
```

