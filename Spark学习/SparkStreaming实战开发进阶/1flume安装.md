# flume安装

**安装flume**

- 下载[flume](http://archive.apache.org/dist/flume/)
- 解压到`/usr/local`目录下
- 重命名为`mv apache-flume-1.5.0-cdh5.3.6-bin flume`
- 配置环境变量

**修改配置文件**

```sh
vi conf/flume-conf.properties

#agent1表示代理名称
agent1.sources=source1
agent1.sinks=sink1
agent1.channels=channel1
#配置source1
agent1.sources.source1.type=spooldir
agent1.sources.source1.spoolDir=/usr/local/flume_logs
agent1.sources.source1.channels=channel1
agent1.sources.source1.fileHeader = false
agent1.sources.source1.interceptors = i1
agent1.sources.source1.interceptors.i1.type = timestamp
#配置channel1
agent1.channels.channel1.type=file
agent1.channels.channel1.checkpointDir=/usr/local/flume_logs_tmp_cp
agent1.channels.channel1.dataDirs=/usr/local/flume_logs_tmp
#配置sink1
agent1.sinks.sink1.type=hdfs
agent1.sinks.sink1.hdfs.path=hdfs://sparkproject1:9000/flume_logs
agent1.sinks.sink1.hdfs.fileType=DataStream
agent1.sinks.sink1.hdfs.writeFormat=TEXT
agent1.sinks.sink1.hdfs.rollInterval=1
agent1.sinks.sink1.channel=channel1
agent1.sinks.sink1.hdfs.filePrefix=%Y-%m-%d
```

**创建需要的文件夹**

```sh
mkdir /usr/local/flume_logs # 本地文件夹
hdfs dfs -mkdir /flume_logs # HDFS文件夹
```

**启动flume-agent**

```sh
flume-ng agent -n agent1 -c conf -f /usr/local/flume/conf/flume-conf.properties -Dflume.root.logger=DEBUG,console
```

**测试flume**

- 新建一份文件，移动到`/usr/local/flume_logs`目录下，flume就会自动上传到HDFS的`/flume_logs`目录中