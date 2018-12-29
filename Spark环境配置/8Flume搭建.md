# Flume搭建

Flume是Cloudera提供的一个高可用的，高可靠的，分布式的海量日志采集、聚合和传输的系统，Flume支持在日志系统中定制各类数据发送方，用于收集数据；同时，Flume提供对数据进行简单处理，并写到各种数据接受方（可定制）的能力。

当前Flume有两个版本Flume 0.9X版本的统称Flume-og，Flume1.X版本的统称Flume-ng。由于Flume-ng经过重大重构，与Flume-og有很大不同，使用时请注意区分。

## 安装Flume

- 下载[flume](http://archive.apache.org/dist/flume/)
- 将下载的hadoop包解压缩到`/usr/local`文件夹下
- 配置hadoop环境变量

## 配置flume

- 修改文件

```sh
vi /usr/local/flume/conf/flume-conf.properties

#agent1表示代理名称
agent1.sources=source1
agent1.sinks=sink1
agent1.channels=channel1
#配置source1
agent1.sources.source1.type=spooldir
agent1.sources.source1.spoolDir=/usr/local/logs
agent1.sources.source1.channels=channel1
agent1.sources.source1.fileHeader = false
agent1.sources.source1.interceptors = i1
agent1.sources.source1.interceptors.i1.type = timestamp
#配置channel1
agent1.channels.channel1.type=file
agent1.channels.channel1.checkpointDir=/usr/local/logs_tmp_cp
agent1.channels.channel1.dataDirs=/usr/local/logs_tmp
#配置sink1
agent1.sinks.sink1.type=hdfs
agent1.sinks.sink1.hdfs.path=hdfs://sparkproject1:9000/logs
agent1.sinks.sink1.hdfs.fileType=DataStream
agent1.sinks.sink1.hdfs.writeFormat=TEXT
agent1.sinks.sink1.hdfs.rollInterval=1
agent1.sinks.sink1.channel=channel1
agent1.sinks.sink1.hdfs.filePrefix=%Y-%m-%d

```

- 创建需要的文件夹

  ```sh
  mkdir /usr/local/logs # 本地文件夹
  hdfs dfs -mkdir /logs # HDFS文件夹
  ```

- 启动flume

  ```sh
  flume-ng agent -n agent1 -c conf -f /usr/local/flume/conf/flume-conf.properties -Dflume.root.logger=DEBUG,console
  ```

  > 新建一份文件，移动到`/usr/local/logs`目录下，flume就会自动上传到`HDFS`的/logs目录中

