# standalone多作业资源调度

- standalone集群对于同时提交上来的多个作业，仅仅支持FIFO调度策略，也就是先入先出
  默认情况下，集群对多个作业同时执行的支持是不好的，没有办法同时执行多个作业，因为先提交上来的每一个作业都会尝试使用集群中所有可用的cpu资源，此时相当于就是只能支持作业串行起来，一个一个运行了
- 如果我们希望能够支持多作业同时运行，那么就需要调整一下资源参数，我们可以设置spark.cores.max参数，来限制每个作业能够使用的最大的cpu core数量，这样先提交上来的作业不会使用所有的cpu资源，后面提交上来的作业就可以获取到资源，也可以同时并行运行了
- 集群一共有20个节点，每个节点是8核，160 cpu core，那么，如果你不限制每个作业获取的最大cpu资源大小，而且在你spark-submit的时候，或者说，你就设置了num-executors，total-cores，160，此时，你的作业是会使用所有的cpu core资源的

- 如果我们可以通过设置全局的一个参数，让每个作业最多只能获取到一部分cpu core资源
  那么，后面提交上来的作业，就也可以获取到一部分资源，standalone集群，才可以支持同时执行多个作业

**使用SparkConf或spark-submit中的--conf标识，设置参数即可**

```scala
SparkConf conf = new SparkConf()
.set("spark.cores.max", "10")
```

- 通常不建议使用SparkConf，硬编码，来设置一些属性，不够灵活，建议使用spark-submit来设置属性

```sh
--conf spark.cores.max=10
```

- 还可以直接通过spark-env.sh配置每个application默认能使用的最大cpu数量来进行限制，默认是无限大，此时就不需要每个application都自己手动设置了，在spark-env.sh中配置`spark.deploy.defaultCores`即可

  ```sh
  export SPARK_MASTER_OPTS="-Dspark.deploy.defaultCores=10"
  ```

