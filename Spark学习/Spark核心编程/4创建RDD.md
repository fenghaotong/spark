# 创建RDD

## 创建RDD

- 进行Spark核心编程时，首先要做的第一件事，就是创建一个初始的RDD。该RDD中，通常就代表和包含了Spark应用程序的输入源数据。然后在创建了初始的RDD之后，才可以通过Spark Core提供的transformation算子，对该RDD进行转换，来获取其他的RDD。
- Spark Core提供了三种创建RDD的方式，包括：使用程序中的集合创建RDD；使用本地文件创建RDD；使用HDFS文件创建RDD。

> - 使用程序中的集合创建RDD，主要用于进行测试，可以在实际部署到集群运行之前，自己使用集合构造测试数据，来测试后面的spark应用的流程。
>
> - 使用本地文件创建RDD，主要用于临时性地处理一些存储了大量数据的文件。
>
> - 使用HDFS文件创建RDD，应该是最常用的生产环境处理方式，主要可以针对HDFS上存储的大数据，进行离线批处理操作。

## 并行化集合创建RDD

- 如果要通过并行化集合来创建RDD，需要针对程序中的集合，调用SparkContext的`parallelize()`方法。Spark会将集合中的数据拷贝到集群上去，形成一个分布式的数据集合，也就是一个RDD。相当于是，集合中的部分数据会到一个节点上，而另一部分数据会到其他节点上。然后就可以用并行的方式来操作这个分布式数据集合，即RDD。

```java
// java版
package cn.spark.study.core;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

public class ParallelizeCollection {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("ParallelizeCollection")
				.setMaster("local");
		
		// 创建JavaSparkContext
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 要通过并行化的方式创建RDD，那么就是要调用SparkContext以及其子类的parallelize()方法
		List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		JavaRDD<Integer> numberRDD = sc.parallelize(numbers);
		
		//执行reduce算子操作
		// 相当于，先进行1 + 2 = 3；然后再用3 + 3 = 6；然后再用6 + 4 = 10...一次类推
		int sum = numberRDD.reduce(new Function2<Integer, Integer, Integer>(){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Integer call(Integer num1, Integer num2) throws Exception{
				return num1 + num2;
			}
		});
		// 输出
		System.out.println("1 to 10 sum : " + sum);
		
		// 关闭JavaSparkContext
		sc.close();
	}
}

```



```scala
// scala版
val arr = Array(1,2,3,4,5,6,7,8,9,10)
val rdd =sc.parallelize()
val sum = rdd.reduce(_ + _)
```

- 调用`parallelize()`时，有一个重要的参数可以指定，就是要将集合切分成多少个partition。Spark会为每一个partition运行一个task来进行处理。Spark官方的建议是，为集群中的每个CPU创建2~4个partition。Spark默认会根据集群的情况来设置partition的数量。但是也可以在调用`parallelize()`方法时，传入第二个参数，来设置RDD的partition数量。比如`parallelize(arr,10)`

## 使用本地文件和HDFS创建RDD

- Spark是支持使用任何Hadoop支持的存储系统上的文件创建RDD的，比如说HDFS、Cassandra、HBase以及本地文件。通过调用SparkContext的textFile()方法，可以针对本地文件或HDFS文件创建RDD。

有几个事项是需要注意的：

> - 如果是针对本地文件的话，如果是在windows上本地测试，windows上有一份文件即可；如果是在spark集群上针对linux本地文件，那么需要将文件拷贝到所有worker节点上。
>
> - Spark的textFile()方法支持针对目录、压缩文件以及通配符进行RDD创建。
>
> - Spark默认会为hdfs文件的每一个block创建一个partition，但是也可以通过`textFile()`的第二个参数手动设置分区数量，只能比block数量多，不能比block数量少。

- Spark的`textFile()`除了可以针对上述几种普通的文件创建RDD之外，还有一些特列的方法来创建RDD：



  > - `SparkContext.wholeTextFiles()`方法，可以针对一个目录中的大量小文件，返回`<filename, fileContent>`组成的pair，作为一个PairRDD，而不是普通的RDD。普通的`textFile()`返回的RDD中，每个元素就是文件中的一行文本。
  > - `SparkContext.sequenceFile[K, V]()`方法，可以针对SequenceFile创建RDD，`K和V`泛型类型就是SequenceFile的key和value的类型。K和V要求必须是Hadoop的序列化类型，比如`IntWritable、Text`等。
  > - `SparkContext.hadoopRDD()`方法，对于Hadoop的自定义输入类型，可以创建RDD。该方法接收JobConf、InputFormatClass、Key和Value的Class
  > - `SparkContext.objectFile()`方法，可以针对之前调用`RDD.saveAsObjectFile()`创建的对象序列化的文件，反序列化文件中的数据，并创建一个RDD。

