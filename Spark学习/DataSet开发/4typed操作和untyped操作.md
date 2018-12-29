# typed操作和untyped操作

### typed操作

**coalesce、repartition**

- 都是用来重新定义分区的
-  区别在于：coalesce，只能用于减少分区数量，而且可以选择不发生shuffle
- repartiton，可以增加分区，也可以减少分区，必须会发生shuffle，相当于是进行了一次重分区操作

**distinct、dropDuplicates**

- 都是用来进行去重的，区别在哪儿呢？
- distinct，是根据每一条数据，进行完整内容的比对和去重
- dropDuplicates，可以根据指定的字段进行去重

**except、filter、intersect**

- except：获取在当前dataset中有，但是在另外一个dataset中没有的元素
- filter：根据我们自己的逻辑，如果返回true，那么就保留该元素，否则就过滤掉该元素
- intersect：获取两个数据集的交集

**map、flatMap、mapPartitions**

- map：将数据集中的每条数据都做一个映射，返回一条新数据
- flatMap：数据集中的每条数据都可以返回多条数据
- mapPartitions：一次性对一个partition中的数据进行处理

**joinWith**

**sort**

**randomSplit、sample**

[Scala](src/TypedOperation.scala)

### untyped操作

**select、where、groupBy、agg、col、join**

[Scala](src/UntypedOperation.scala)