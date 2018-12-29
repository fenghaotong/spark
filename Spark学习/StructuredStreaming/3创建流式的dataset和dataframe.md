# 创建流式的dataset和dataframe

流式dataframe可以通过DataStreamReader接口来创建，DataStreamReader对象是通过SparkSession的readStream()方法返回的。与创建静态dataframe的read()方法类似，我们可以指定数据源的一些配置信息，比如data format、schema、option等。spark 2.0中初步提供了一些内置的source支持。

- file source：以数据流的方式读取一个目录中的文件。支持text、csv、json、parquet等文件类型。文件必须是被移动到目录中的，比如用mv命令。
- socket source：从socket连接中读取文本内容。driver是负责监听请求的server socket。socket source只能被用来进行测试。

**对流式的dataset和dataframe执行计算操作**

![](img\滑动窗口：基于event-time.png)



**不支持的操作**

- streaming dataframe的chain aggregation
- limit and take
- distinct
- sort：仅在聚合过后，同时使用complete output mode时可用
- streaming dataframe和static dataframe的outer join
  - full outer join是不支持的
  - streaming dataframe在左侧时，left outer join是不支持的
  - streaming dataframe在右侧时，right outer join是不支持的
- 两个streaming dataframe的join是不支持的
- count() -> groupBy().count()
- foreach() -> df.writeStream.foreach()
- show() -> console output sink