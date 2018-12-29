# CentOS集群搭建

[CentOS设置163的yum源的过程](https://jingyan.baidu.com/article/27fa73269cccfe46f8271f0b.html)

```sh
cd /etc/yum.repos.d/

rm -rf *

cp /usr/local/CentOS6-Base-163.repo . # 自己的repo文件移动到/etc/yum.repos.d/目录中：cp /usr/local/CentOS6-Base-163.repo .

# 修改repo文件，把所有gpgcheck属性修改为0

# 2、配置yum

yum clean all

yum makecache

yum install telnet
```

## 使用虚拟机搭建台Linux系统

- 准备镜像[CentOS-6.4-i386-minimal.iso](http://vault.centos.org/)
- 使用VMware
- 创建Linux系统的虚拟机
- 打开虚拟机的`虚拟网络编辑器`，设置`NAT模式`的`子网IP`，我的子网设置的`192.168.75.0`如下图

![](img\虚拟网络编辑器.png)

- 设置网络为`NAT`模式
- 打开虚拟机，配置网络，打开`cd /etc/sysconfig/network-scripts/ifcfg-eth0`文件，修改如下

```sh

DEVICE=eth0
HWADDR=00:0C:29:96:86:00
TYPE=Ethernet
UUID=3d0e97dd-4e4c-4cd2-8450-b9167bfd0410
ONBOOT=yes
NM_CONTROLLED=yes
BOOTPROTO=static
IPADDR=192.168.75.111
NETMASK=255.255.255.0
GATEWAY=192.168.75.2
DNS1=192.168.75.2

```

- 关闭防火墙

```sh
service iptables stop
service ip6tables stop
chkconfig iptables off
chkconfig ip6tables off


vi /etc/selinux/config
SELINUX=disabled
```

> 关闭windows的防火墙！

- 修改`vi /etc/resolv.conf`

```sh
nameserver 192.168.75.2
```

- 修改`vi /etc/hosts`，映射

```sh
192.168.75.111 spark1
```

- 重启网络

```sh
service network restart
```

- 测试网络

```sh
ping www.baidu.com
```

- 安装JDK，配置环境变量（这个不详细说明）
- 安装上述步骤，再安装两台一模一样环境的虚拟机，因为后面hadoop和spark都是要搭建集群的。
- 集群的最小环境就是三台。因为后面要搭建ZooKeeper、kafka等集群。
- 另外两台机器的hostname分别设置为spark2和spark3即可，ip分别为192.168.75.112和192.168.75.113
- 安装好之后，记得要在三台机器的/etc/hosts文件中，配置全三台机器的ip地址到hostname的映射，而不能只配置本机，这个很重要！

```sh
192.168.75.111 spark1
192.168.75.112 spark2
192.168.75.113 spark3
```

- 在windows的hosts文件中也要配置全三台机器的ip地址到hostname的映射。

## 集群ssh免密登陆

- 在每台虚拟机上执行

```sh
ssh-keygen -t rsa
cd /root/.ssh
cp id_rsa.pub authorized_keys
```

- 配置三台机器互相之间的ssh免密码登陆，以在`spark1`机器上为例，另外两台类似

```sh
ssh-copy-id -i spark2
ssh-copy-id -i spark3
```

