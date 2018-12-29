# Hadoop集群搭建

## 安装hadoop

- 下载[hadoop](http://archive.apache.org/dist/hadoop/core/)
- 将下载的hadoop包解压缩到`/usr/local`文件夹下
- 配置hadoop环境变量

## 配置hadoop

```sh
cd hadoop/etc/hadoop/
```

### 修改`core-site.xml`

```xml
<property>
  <name>fs.default.name</name>
  <value>hdfs://spark1:9000</value>
</property>

```

###  修改`hdfs-site.xml`

```xml
<property>
  <name>dfs.name.dir</name>
  <value>/usr/local/data/namenode</value>
</property>
<property>
  <name>dfs.data.dir</name>
  <value>/usr/local/data/datanode</value>
</property>
<property>
  <name>dfs.tmp.dir</name>
  <value>/usr/local/data/tmp</value>
</property>
<property>
  <name>dfs.replication</name>
  <value>3</value>
</property>

```

### 修改`mapred-site.xml`

```xml
<property>
  <name>mapreduce.framework.name</name>
  <value>yarn</value>
</property>

```

### 修改`yarn-site.xml`

```xml
<property>
  <name>yarn.resourcemanager.hostname</name>
  <value>spark1</value>
</property>
<property>
  <name>yarn.nodemanager.aux-services</name>
  <value>mapreduce_shuffle</value>
</property>

```

### 修改`slaves`

```xml
spark1
spark2
spark3
```

## 启动hadoop

#### 配置另外两台

- 使用如上配置在另外两台机器上搭建`hadoop`，可以使用`scp`命令将`spark1`上面的`hadoop`安装包和`.bashrc`配置文件都拷贝过去。
- 要记得对`.bashrc`文件进行`source`，以让它生效。
- 记得在`spark2`和`spark3`的`/usr/local`目录下创建`data`目录。

#### 启动hdfs集群

- 格式化`namenode`：在`spark1`上执行以下命令，`hdfs namenode -format`
- 启动hdfs集群：`start-dfs.sh`
- 验证启动是否成功：`jps、50070`端口

```sh
spark1：namenode、datanode、secondarynamenode

spark2：datanode

spark3：datanode
```

#### 启动yarn集群

- 启动yarn集群：`start-yarn.sh`
- 验证启动是否成功：`jps、8088`端口

```sh
spark1：resourcemanager、nodemanager

spark2：nodemanager

spark3：nodemanager
```







