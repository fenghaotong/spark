# Master原理剖析与源码分析



## 主备切换机制原理剖析

- Master实际上可以配置两个，那么Spark原生standalone模式是支持Master主备切换的，也即是说，当Active Master节点挂掉的时候，我们可以将Standby Master切换为Active Master
- Spark Master主备切换可以基于两种机制：一种是基于文件系统的，一种是基于Zookeeper的。基于文件系统的主备切换机制，需要在Active Master挂掉之后，由我们手动去切换到Standy Master；而基于Zookeeper的主备切换机制，可以实现自动切换Master。
- 当Active Master挂掉之后，切换到Standby Master时，Master会做那些操作。

![](img\主备切换机制原理剖析.png)

## 注册机制原理剖析

![](../img/注册机制原理剖析.png)

## 状态改变处理机制源码分析



## 资源调度机制源码分析（schedule()，两种资源调度算法）

