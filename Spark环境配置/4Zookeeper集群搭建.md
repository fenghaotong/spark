# ZooKeeper集群搭建

搭建`Zookeeper`的目的是为了后面搭建`kafka`，搭建`kafka`的目的是后面的`spark streaming`要进行实时计算，最常用的场景就是让`Spark streaming`接通`kafka`来做实时计算的实验。

## 下载安装ZooKeeper

- 下载[ZooKeeper](http://archive.apache.org/dist/zookeeper/)
- 将下载的`ZooKeeper`包解压缩到`/usr/local`文件夹下
- 修改`ZooKeeper`文件夹名字为`zk`
- 配置环境变量

## 配置ZooKeeper

### 配置zoo.cfg

```sh
cd zk/conf
mv zoo_sample.cfg zoo.cfg
vi zoo.cfg

修改：dataDir=/usr/local/zk/data
新增：
server.0=spark1:2888:3888	
server.1=spark2:2888:3888
server.2=spark3:2888:3888
```

### 设置zk节点标识

```sh
cd zk
mkdir data
cd data

vi myid
0
```

## 搭建zk集群

- 在另外两个节点上按照上述步骤配置`ZooKeeper`，使用`scp`将`zk`和`.bashrc`拷贝到`spark2`和`spark3`上即可。
- 唯一的区别是`spark2`和`spark3`的标识号分别设置为`1`和`2`。

## 启动ZooKeeper集群

- 分别在三台机器上执行：`zkServer.sh start`。

> 会产生`QuorumPeerMain`进程

- 检查`ZooKeeper`状态：`zkServer.sh status`。
- 进入`ZooKeeper`命令行：`zkCli.sh`。

