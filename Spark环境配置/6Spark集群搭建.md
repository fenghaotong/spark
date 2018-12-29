# Spark集群搭建

## Spark安装

- 下载[Spark-bin-hadoop](https://archive.apache.org/dist/spark/)
- 将下载的`Spark-bin-hadoop`包解压缩到`/usr/local`文件夹下
- 修改`Spark-bin-hadoop`文件夹名字为`spark`
- 配置环境变量

```sh
vi .bashrc
export SPARK_HOME=/usr/local/spark
export PATH=$PATH:$SPARK_HOME/bin
export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib:$JAVA_HOME/jre/lib
source .bashrc
```

## 配置Spark

- 修改`spark-env.sh`

```sh
cd /usr/local/spark/conf
cp spark-env.sh.template spark-env.sh
vi spark-env.sh

export JAVA_HOME=/usr/java/latest
export SCALA_HOME=/usr/local/scala
export SPARK_MASTER_IP=192.168.75.111
export SPARK_WORKER_MEMORY=1g
export HADOOP_CONF_DIR=/usr/local/hadoop/etc/hadoop
```

- 修改`slaves`文件

```sh
spark1
spark2
spark3
```

## 安装Spark集群

- 在另外两个节点进行一模一样的配置，使用`scp`将`spark`和`.bashrc`拷贝到`spark2`和`spark3`即可。

## 启动Spark集群

- 在`spark`目录下的`sbin`目录
- 执行`./start-all.sh`
- 使用`jsp和8080`端口可以检查集群是否启动成功
- 进入`spark-shell`查看是否正常



### Spark 2.0集群搭建

**配置spark可以使用hive**

1. 将hive-site.xml放置到spark的conf目录下

2. 修改spark/conf和hive/conf下的hive-site.xml

   ```xml
   <property>
   
     <name>hive.metastore.uris</name>
   
     <value>thrift://spark2upgrade01:9083</value>
   
   </property>
   ```

3. 启动hive metastore service

   ```sh
   hive --service metastore &
   ```

4. `cp hive/lib/mysql-connector-java-5.1.17.jar spark/jars/`

5. `hdfs dfs -chmod 777 /tmp/hive-root`





