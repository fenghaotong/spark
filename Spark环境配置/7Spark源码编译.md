# Spark 源码编译

- 掌握了源码编译，就具备了对Spark进行二次开发的基本条件了！如果你要修改Spark源码，进行二次开发，那么首先就得从官网下载指定版本的源码，然后倒入你的ide开发环境，进行源码的修改；接着修改完了，你希望能够将修改后的源码部署到集群上面去，那么是不是得对源码进行编译，编译成可以在linux集群上进行部署的格式包吧！

**编译过程**

- 下载[spark源码](https://archive.apache.org/dist/spark/)如（`spark-x.x.x.tgz`）
- 准备好`JDK、Scala、Maven`环境
- 针对指定`hadoop`版本进行编译

```sh
./make-distribution.sh --tgz -Phadoop-2.6 -Pyarn -DskipTests -Dhadoop.version=2.6.0 -Phive
```

- 经常长时间的编译之后，得到`spark-x.x.x-bin-2.6.0.tgz`

