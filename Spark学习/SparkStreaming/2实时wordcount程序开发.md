# 实时wordcount程序开发



- 安装nc工具：yum install nc

> 如果遇到错误`nc: Protocol not available`，是因为版本太高
>
> 删除原来的`nc`
>
> ```sh
> yum erase nc
> ```
>
> 下载较低版本的nc的.rpm文件
>
> ```sh
> wget http://vault.centos.org/6.3/os/i386/Packages/nc-1.84-22.el6.i686.rpm
> ```
>
> 安装
>
> ```sh
> rpm -iUv nc-1.84-22.el6.i686.rpm
> ```
>
> 测试
>
> ```sh
> nc -lk 9999
> ```

- 开发实时wordcount程序

[Java版本实例](src/java/wordcount.java)

[Scala版本实例](src/scala/WordCount.scala)