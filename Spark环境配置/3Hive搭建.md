#  Hive搭建

> 仅仅在spark1上搭建

## 下载安装HIVE

- 下载[hive](http://archive.apache.org/dist/hive/)，下载`bin`版本，不要下载`src`版本
- 将下载的`hive`包解压缩到`/usr/local`文件夹下
- 修改夹名字为`hive`
- 配置环境变量

## 下载安装`mysql`

- 安装`mysql server`

```sh
yum install -y mysql-server
service mysqld start
chkconfig mysqld on
```

- 安装`mysql connector`

```sh
yum install -y mysql-connector-java
```

- 将`mysql connector`拷贝到`hive`的`lib`包中

```sh
cp /usr/share/java/mysql-connector-java-5.1.17.jar /usr/local/hive/lib
```

- 在mysql上创建hive元数据库，并对hive进行授权

```sh
create database if not exists hive_metadata;
grant all privileges on hive_metadata.* to 'hive'@'%' identified by 'hive';
grant all privileges on hive_metadata.* to 'hive'@'localhost' identified by 'hive';
grant all privileges on hive_metadata.* to 'hive'@'spark1' identified by 'hive';
flush privileges;
use hive_metadata;
```

## 配置`hive-site.xml`

```sh
cd /hive/conf
mv hive-default.xml.template hive-site.xml
vi hive-site.xml
```

- 修改`hive-site.xml`相应内容

```xml
<property>
  <name>javax.jdo.option.ConnectionURL</name>
  <value>jdbc:mysql://spark1:3306/hive_metadata?createDatabaseIfNotExist=true</value>
</property>
<property>
  <name>javax.jdo.option.ConnectionDriverName</name>
  <value>com.mysql.jdbc.Driver</value>
</property>
<property>
  <name>javax.jdo.option.ConnectionUserName</name>
  <value>hive</value>
</property>
<property>
  <name>javax.jdo.option.ConnectionPassword</name>
  <value>hive</value>
</property>
<property>
  <name>hive.metastore.warehouse.dir</name>
  <value>/user/hive/warehouse</value>
</property>
```

## 配置`hive-env.sh`和`hive-config.sh`

```sh
cd /hive/conf
mv hive-env.sh.template hive-env.sh

vi /usr/local/hive/bin/hive-config.sh
```

- 修改`hive-config.sh`

```sh
export JAVA_HOME=/usr/java/latest
export HIVE_HOME=/usr/local/hive
export HADOOP_HOME=/usr/local/hadoop
```

## 验证hive是否安装成功

- 直接输入`hive`命令，可以进入`hive`命令行

- 如果出现以下错误：

```sh
[Fatal Error] hive-site.xml:2787:3: The element type "configuration" must be terminated by the matching end-tag "</configuration>".

...
```

- 原因是`hive-site.xml`文件`2783`行少了一个`<porperty>`，打开文件添加
- 测试`hive`

```sh
[root@spark1 conf]# hive
18/11/18 08:00:06 WARN conf.HiveConf: DEPRECATED: hive.metastore.ds.retry.* no longer has any effect.  Use hive.hmshandler.retry.* instead

Logging initialized using configuration in jar:file:/usr/local/hive/lib/hive-common-0.13.1-cdh5.3.6.jar!/hive-log4j.properties
hive> show databases;
OK
default
Time taken: 0.413 seconds, Fetched: 1 row(s)
hive> create table t1(id int);
OK
Time taken: 0.786 seconds
hive> drop table t1;
OK
Time taken: 0.559 seconds

```



