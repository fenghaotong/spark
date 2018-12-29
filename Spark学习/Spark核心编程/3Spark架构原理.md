# Spark架构原理

## Driver（进程）

- 我们编写的Spark程序就在Driver上，由Driver进程执行

> Spark集群的节点之一，或者提交spark程序的机器

## Master

- 是个进程，其实主要是负责资源的调度和分配，还有集群的监控等职责。

## Worker

- worker是个进程，主要负责两个，一个使用自己的内存存储RDD的某个或某些partition，另一个是启动其他进程或者线程，对RDD上的partition进行并行的处理和计算。

## Executor（进程），Task（线程）

- Exceutor和Task，其实就是负责执行，对RDD的partition进行并行的计算，也就是执行我们对RDD定义的，比如map、flatmap、reduce等原子操作。

## 工作流程

- Driver进程启动后，会做一些初始化的操作，在这个过程中，就会发送请求到Master上，进行Spark应用程序的注册，说白了，就是让Master知道，有一个新的Spark应用程序要运行
- Master，在接收到了Spark应用程序的注册申请后，会发送请求给worker，进行资源的调度和分配，资源分配就是executor分配
- worker，接收到Master的请求之后，会为spark应用启动Executor。
- Executor启动之后，回向Driver进行反注册，这样Driver就知道，那些Exceutor是为它服务了。
- Driver注册了一些executor之后，就可以开始正式执行我们的spark应用程序了，首先第一步，就是创建初始RDD，读取数据源。
- HDFS文件内容被读取到多个worker节点上，形成内存中的分布式数据集，也就是初始RDD。
- Driver会根据我们对RDD定义的操作，提交一大堆task去Exceutor
- Exceutor接收到task之后，会启动多个线程来执行task。
- task就会对RDD的partition数据执行指定的算子操作，形成新的RDD的partition。

![](..\img\Spark架构原理.png)