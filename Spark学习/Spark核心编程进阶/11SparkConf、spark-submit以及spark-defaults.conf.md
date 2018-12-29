# SparkConf、spark-submit以及spark-defaults.conf

- spark-submit脚本会自动加载conf/spark-defaults.conf文件中的配置属性，并传递给我们的spark应用程序
- 加载默认的配置属性，一大好处就在于，我们不需要在spark-submit脚本中设置所有的属性
- 比如说，默认属性中有一个spark.master属性，所以我们的spark-submit脚本中，就不一定要显式地设置--master，默认就是local
- spark配置的优先级如下: SparkConf、spark-submit、spark-defaults.conf

```sh
# 假设设置spark.default.parallelism参数,则采用SparkConf.set("spark.default.parallelism", "100")

SparkConf.set("spark.default.parallelism", "100")
spark-submit: --conf spark.default.parallelism=50
spark-defaults.conf: spark.default.parallelism 10
```

> 虽然说SparkConf设置属性的优先级是最高的，但是有的时候咱们可能不希望在代码中硬编码一些配置属性，否则每次修改了参数以后，还得去代码里修改，然后得重新打包应用程序，再部署到生产机器上去，非常得麻烦
>
> 所以一般使用spark-submit设置属性

- 在代码中仅仅创建一个空的SparkConf对象，比如:` val sc = new SparkContext(new SparkConf())`，然后可以在spark-submit脚本中，配置各种属性的值，比如:

  ```sh
  ./bin/spark-submit \
    --name "My app" \
    --master local[4] \
    --conf spark.shuffle.spill=false \
    --conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps" \
    myApp.jar
  ```

  > spark.shuffle.spill, 如果是在代码中，`SparkConf.set("spark.shuffle.spill", "false")`来配置的
  > 此时在spark-submit中配置了，不需要更改代码，就可以更改属性，非常得方便，尤其是对于spark程序的调优，格外方便，因为调优说白了，就是不断地调整各种各样的参数，然后反复跑反复试的过程

**spark的属性配置方式**

- spark-shell和spark-submit两个工具，都支持两种加载配置的方式
- 一种是基于命令行参数，比如上面的--master，spark-submit可以通过--conf参数，接收所有spark属性
- 另一种是从conf/spark-defaults.conf文件中加载，其中每一行都包括了一个key和value
  比如spark.executor.memory 4g
- 所有在SparkConf、spark-submit和spark-defaults.conf中配置的属性，在运行的时候，都会被综合使用，直接通过SparkConf设置的属性，优先级是最高的，会覆盖其余两种方式设置的属性，其次是spark-submit脚本中通过--conf设置的属性，最后是spark-defaults.conf中设置的属性
- 通常来说，如果你要对所有的spark作业都生效的配置，放在spark-defaults.conf文件中，只要将spark-defaults.conf.template拷贝成那个文，然后在其中编辑即可，对于某个spark作业比较特殊的配置，推荐放在spark-submit脚本中，用--conf配置，比较灵活
  SparkConf配置属性，有什么用呢？也有用，在eclipse中用local模式执行运行的时候，那你就只能在SparkConf中设置属性了
- 在新的spark版本中，可能会将一些属性的名称改变，那些旧的属性名称就变成过期的了，此时旧的属性名称还是会被接受的，但是新的属性名称会覆盖掉旧的属性名称，并且优先级是比旧属性名称更高的

举例来说

```
shuffle reduce read操作的内存缓冲块儿

spark 1.3.0: spark.reducer.maxMbInFlight
spark 1.5.0: spark.reducer.maxSizeInFlight
```



