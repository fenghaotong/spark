# spark-submit配置第三方依赖

- 使用spark-submit脚本提交spark application时，application jar，还有我们使用--jars命令绑定的其他jar，都会自动被发送到集群上去

**spark支持以下几种URL来指定关联的其他jar**

- file: 是由driver的http文件服务提供支持的，所有的executor都会通过driver的HTTP服务来拉取文件
- hdfs:，http:，https:，ftp:，这种文件，就是直接根据URI，从指定的地方去拉取，比如hdfs、或者http链接、或者ftp服务器
- local: 这种格式的文件必须在每个worker节点上都要存在，所以不需要通过网络io去拉取文件，这对于特别大的文件或者jar包特别适用，可以提升作业的执行性能

**--jar和--file**

- 文件和jar都会被拷贝到每个executor的工作目录中，这就会占用很大一片磁盘空间，因此需要在之后清理掉这些文件，在yarn上运行spark作业时，依赖文件的清理都是自动进行的，用standalone模式，需要配置spark.worker.cleanup.appDataTtl属性，来开启自动清理依赖文件和jar包

- 用户还可以通过在spark-submit中，使用--packages，绑定一些maven的依赖包，此外，还可以通过--repositories来绑定过一些额外的仓库，但是这两种情况还的确不太常见

- `--files`，比如，最典型的就是`hive-site.xml`配置文件

- `--jars`，比如，mysql驱动包，或者是其他的一些包

