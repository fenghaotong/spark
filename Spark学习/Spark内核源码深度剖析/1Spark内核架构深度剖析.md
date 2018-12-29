# Spark内核架构深度剖析

## Spark内核架构

### Application

- 提交的Spark程序的机器

### spark-submit

- spark-submit提交Spark程序
- Spark-submit使用之前一直使用的那种提交模式提交，叫做Standalone，其实会通过反射的方式，创建和构造一个DriverActor进行

### Driver

- 启动一个进程
- 执行Application应用程序
- 执行构造SparkConf和SparkContext

### SparkContext

- 初始化时，做的最重要的事情就是构造DAGScheduler和TaskScheduler
- TaskScheduler通过它对应的一个后台进程，连接Master，向Master注册Application
- 所有Executor都反响注册到Driver上之后，Driver结束SparkContext初始化，会继续执行我们自己编写的代码。
- 每执行到一个action，就会创建一个job。

### Master

- Master接收到Application注册的请求后，会使用自己的资源调度算法，再Spark集群的Worker上，为这个Application启动多个Executor（Master通知Worker启动Executor）

### Worker

- Worker上，为这个Application启动多个Executor

### Executor

- Executor启动之后会自己反向注册到TaskScheduler上去
- Executor没接收到一个task，都会用TaskRunner来封装task，然后从线程池里取出一个线程，执行这个task。
- TaskRunner，将编写的代码，拷贝，反序列化，然后执行Task。

### Job

- Job会提交给DAGScheduler

### DAGScheduler

- DAGScheduler会将job划分为多个stage，然后每个stage创建一个TaskSet。（stage划分算法）
- 每个TaskSet会给每个TaskScheduler。

### TaskScheduler

- TaskScheduler会把TaskSet里面的每个Task提交到Executor上执行（task分配算法）

### ShuffleMapTask and ResultTask

- task有两种ShuffleMapTask 和ResultTask
- 只有最后一个stage时ResultTask，之前的stage都是ShuffleMapTask。
- 最后整个spark应用程序的执行，就是stage分批次作为taskset提交到Executor执行，每个task针对RDD的一个Partition，执行我们定义的算子和函数，以此类推，直到所有操作执行完为止。





![](img\Spark内核架构深度剖析.png)

