# 基于Yarn的两种提交模式深度剖析

- Spark内核架构，其实就是第一种模式，standalone模式，基于Spark自己的Master-Worker集群。
- 第二种，是基于YARN的yarn-cluster模式。
- 第三种，是基于YARN的yarn-client模式。
- 如果，你要切换到第二种和第三种模式，很简单，将我们之前用于提交spark应用程序的spark-submit脚本，加上--master参数，设置为yarn-cluster，或yarn-client，即可。如果你没设置，那么，就是standalone模式。

![](img\基于YARN的两种提交模式深度剖析.png)

- yarn-client:用于测试，因为driver运行在本地客户端，负责调度Application，会与yarn集群产生超大量的网络通信，从而导致网卡流量激增。好处在于，直接执行是，本地可以看到所有的log文件，方便调试
- yarn-cluster，用于生产环境，以为driver运行在nodemanager，没有网卡流量激增，缺点在于，调试不方便，本地用spark-submit提交后，看不到log，只能通过yarn application -logs application_id这种命令来查看，很麻烦

在`spark/conf/spark-env.sh`文件中添加：

```sh
# 使用--master yarn-client/yarn-cluster
export export HADOOP_HOME=/usr/local/hadoop
```



