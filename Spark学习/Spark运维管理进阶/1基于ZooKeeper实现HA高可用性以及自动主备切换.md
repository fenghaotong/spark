# 基于ZooKeeper实现HA高可用性以及自动主备切换

默认情况下，standalone cluster manager对于worker节点的失败是具有容错性的（迄今为止，Spark自身而言对于丢失部分计算工作是有容错性的，它会将丢失的计算工作迁移到其他worker节点上执行）。然而，调度器是依托于master进程来做出调度决策的，这就会造成单点故障：如果master挂掉了，就没法提交新的应用程序了。为了解决这个问题，spark提供了两种高可用性方案，分别是基于zookeeper的HA方案以及基于文件系统的HA方案。



### 基于zookeeper的HA方案

**概述**

使用zookeeper来提供leader选举以及一些状态存储，你可以在集群中启动多个master进程，让它们连接到zookeeper实例。其中一个master进程会被选举为leader，其他的master会被指定为standby模式。如果当前的leader master进程挂掉了，其他的standby master会被选举，从而恢复旧master的状态。并且恢复作业调度。整个恢复过程（从leader master挂掉开始计算）大概会花费1~2分钟。要注意的是，这只会推迟调度新的应用程序，master挂掉之前就运行的应用程序是不被影响的。



**配置**

如果要启用这个恢复模式，需要在spark-env.sh文件中，设置SPARK_DAEMON_JAVA_OPTS选项：

```sh
spark.deploy.recoveryMode		# 设置为ZOOKEEPER来启用standby master恢复模式（默认为NONE）
spark.deploy.zookeeper.url		# zookeeper集群url（举例来说，192.168.75.101:2181,192.168.75.102:2181）
spark.deploy.zookeeper.dir		# zookeeper中用来存储恢复状态的目录（默认是/spark）
```

> 备注：如果在集群中启动了多个master节点，但是没有正确配置master去使用zookeeper，master在挂掉进行恢复时是会失败的，因为没法发现其他master，并且都会认为自己是leader。这会导致集群的状态不是健康的，因为所有master都会自顾自地去调度。

**细节**

- 在启动一个zookeeper集群之后，启用高可用性是很直接的。简单地在多个节点上启动多个master进程，并且给它们相同的zookeeper配置（zookeeper url和目录）。master就可以被动态加入master集群，并可以在任何时间被移除掉。
- 为了调度新的应用程序或者向集群中添加worker节点，它们需要知道当前leader master的ip地址。这可以通过传递一个master列表来完成。举例来说，我们可以将我们的SparkContext连接的地址指向`spark://host1:port1,host2:port2`。这就会导致你的SparkContext尝试去注册所有的master，如果host1挂掉了，那么配置还是正确的，因为会找到新的leader master，也就是host2。
- 对于注册一个master和普通的操作，这是一个重要的区别。当一个应用程序启动的时候，或者worker需要被找到并且注册到当前的leader master的时候。一旦它成功注册了，就被保存在zookeeper中了。如果故障发生了，new leader master会去联系所有的之前注册过的应用程序和worker，并且通知它们master的改变。这样的话，它们甚至在启动的时候都不需要知道new master的存在。
- 正是由于这个属性，new master可以在任何时间被创建，并且我们唯一需要担心的一件事情就是新的应用程序和worker可以找到并且注册到master。一旦注册上去之后，我们就不用担心它了。

**实验**

- 将192.168.75.101机器上的spark集群先停止

  ```sh
  ./sbin/stop-all.sh
  ```

- 修改机器上的spark-env.sh文件，在其中加入上述三个属性

  ```sh
  export SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=192.168.75.101:2181,192.168.75.102:2181 -Dspark.deploy.zookeeper.dir=/spark"
  ```

- 启动集群
  在192.168.75.101上直接用启动集群：`./sbin/start-all.sh`

- 在192.168.75.102上部署spark安装包，并启动一个master进程

安装scala 2.11.4

1. 将课程提供的scala-2.11.4.tgz使用WinSCP拷贝到/usr/local/src目录下。

2. 对scala-2.11.4.tgz进行解压缩：tar -zxvf scala-2.11.4.tgz

3. 对scala目录进行重命名：mv scala-2.11.4 scala

4. 配置scala相关的环境变量

   ```sh
   vi ~/.bashrc
   export SCALA_HOME=/usr/local/scala
   export PATH=$SCALA_HOME/bin
   source ~/.bashrc
   ```

5. 查看scala是否安装成功：scala -version

安装spark客户端

1. 将spark-1.5.1-bin-hadoop2.4.tgz使用WinSCP上传到/usr/local/src目录下。

2. 解压缩spark包：tar -zxvf spark-1.5.1-bin-hadoop2.4.tgz。

3. 重命名spark目录：mv spark-1.5.1-bin-hadoop2.4 spark

4. 修改spark环境变量

   ```sh
   vi ~/.bashrc
   export SPARK_HOME=/usr/local/spark
   export PATH=$SPARK_HOME/bin
   export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib:$JAVA_HOME/jre/lib
   source ~/.bashrc
   ```

修改spark-env.sh文件

```sh
cd /usr/local/spark/conf
cp spark-env.sh.template spark-env.sh
vi spark-env.sh
export JAVA_HOME=/usr/java/latest
export SCALA_HOME=/usr/local/scala
export HADOOP_HOME=/usr/local/hadoop
export HADOOP_CONF_DIR=/usr/local/hadoop/etc/hadoop
export SPARK_MASTER_IP=192.168.75.102
export SPARK_DAEMON_MEMORY=100m
export SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=192.168.75.101:2181,192.168.75.102:2181 -Dspark.deploy.zookeeper.dir=/spark"
```

在192.168.1.102上单独启动一个standby master进程：`./sbin/start-master.sh`

- 提交应用程序

  将master地址修改为192.168.75.101:7077,192.168.75.102:7078

- 杀掉原先的leader master，等到standby master接管集群，再次提交应用程序

- 再次手动启动原来的leader master（死掉）

### 基于文件系统的HA方案

zookeeper是实现生产级别的高可用性的最佳方式，但是如果你就是想要在master进程挂掉的时候，手动去重启它，而不是依靠zookeeper实现自动主备切换，那么可以使用FILESYSTEM模式。当应用程序和worker都注册到master之后，master就会将它们的信息写入指定的文件系统目录中，以便于当master重启的时候可以从文件系统中恢复注册的应用程序和worker状态。



**配置**

要启用这种恢复模式，需要在spark-env.sh中设置`SPARK_DAEMON_JAVA_OPTS`

```sh
spark.deploy.recoveryMode		# 设置为FILESYSTEM来启用单点恢复（默认值为NONE）
spark.deploy.recoveryDirectory	# spark在哪个文件系统目录内存储状态信息，必须是master可以访问的目录
```

**细节**

1. 这个解决方案可以与进程监控或管理器（比如monit）结合使用，或者就仅仅是启用手动重启恢复机制即可。
2. 文件系统恢复比不做任何恢复机制肯定是要好的，这个模式更加适合于开发和测试环境，而不是生产环境。此外，通过stop-master.sh脚本杀掉一个master进程是不会清理它的恢复状态的，所以当你重启一个新的master进程时，它会进入恢复模式。这会增加你的恢复时间至少1分钟，因为它需要等待之前所有已经注册的worker等节点先timeout。
3. 这种方式没有得到官方的支持，也可以使用一个NFS目录作为恢复目录。如果原先的master节点完全死掉了，你可以在其他节点上启动一个master进程，它会正确地恢复之前所有注册的worker和应用程序。之后的应用程序可以找到新的master，然后注册。

**实验**

1. 关闭两台机器上的master和worker

2. 修改192.168.75.101机器上的spark-env.sh

   ```sh
   export SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=FILESYSTEM -Dspark.deploy.recoveryDirectory=/usr/local/spark_recovery"
   ```

3. 在192.168.75.101上启动spark集群

4. 在spark-shell中进行wordcount计数，到一半，有一个running application

5. 杀掉master进程

6. 重启master进程

7. 观察web ui上，是否恢复了worker以及原先正在运行的application