package cn.spark.study.streaming;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

public class wordcount {
	public static void main(String[] args) throws Exception {
		// 创建SparkConf对象
		// 但是这里有一点不同，我们是要给它设置一个Master属性，但是我们测试的时候使用local模式
		// local后面必须跟一个方括号，里面填写一个数字，数字代表了，我们用几个线程来执行我们的
		// Spark Streaming程序
		SparkConf conf = new SparkConf()
				.setMaster("local[2]")
				.setAppName("wordcount");
		
		// 创建JavaStreamingContext对象
		// 该对象，就类似于Spark Core中的JavaSparkContext，就类似于Spark SQL中的SQLContext
		// 该对象除了接收SparkConf对象对象之外
		// 还必须接收一个batch interval参数，就是说，每收集多长时间的数据，划分为一个batch，进行处理
		// 这里设置一秒
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
		
		// 首先，创建输入DStream，代表了一个从数据源（比如kafka、socket）来的持续不断的实时数据流
		// 调用JavaStreamingContext的socketTextStream()方法，可以创建一个数据源为Socket网络端口的
		// 数据流，JavaReceiverInputStream，代表了一个输入的DStream
		// socketTextStream()方法接收两个基本参数，第一个是监听哪个主机上的端口，第二个是监听哪个端口
		JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
		
		// 到这里为止，你可以理解为JavaReceiverInputDStream中的，每隔一秒，会有一个RDD，其中封装了
		// 这一秒发送过来的数据
		// RDD的元素类型为String，即一行一行的文本
		// 所以，这里JavaReceiverInputStream的泛型类型<String>，其实就代表了它底层的RDD的泛型类型
		
		// 开始对接收到的数据，执行计算，使用Spark Core提供的算子，执行应用在DStream中即可
		// 在底层，实际上是会对DStream中的一个一个的RDD，执行我们应用在DStream上的算子
		// 产生的新RDD，会作为新DStream中的RDD
		JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterable<String> call(String line) throws Exception {
				return Arrays.asList(line.split(" "));
			}
		});
		
		// 这个时候，每秒的数据，一行一行的文本，就会被拆分为多个单词，words DStream中的RDD的元素类型
		// 即为一个一个的单词
		
		// 接着，开始进行flatMap、reduceByKey操作
		JavaPairDStream<String, Integer> pairs = words.mapToPair(
				new PairFunction<String, String, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> call(String word) throws Exception {
						return new Tuple2<String, Integer>(word, 1);
					}
				
				});
		
		// 这里，正好说明一下，其实大家可以看到，用Spark Streaming开发程序，和Spark Core很相像
		// 唯一不同的是Spark Core中的JavaRDD、JavaPairRDD，都变成了JavaDStream、JavaPairDStream
		JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey(
				new Function2<Integer, Integer, Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Integer call(Integer v1, Integer v2) throws Exception {
						// TODO Auto-generated method stub
						return v1 + v2;
					}
				});
		
		// 到此为止，我们就实现了实时的wordcount程序了
		// 大家总结一下思路，加深一下印象
		// 每秒中发送到指定socket端口上的数据，都会被lines DStream接收到
		// 然后lines DStream会把每秒的数据，也就是一行一行的文本，诸如hell world，封装为一个RDD
		// 然后呢，就会对每秒中对应的RDD，执行后续的一系列的算子操作
		// 比如，对lins RDD执行了flatMap之后，得到一个words RDD，作为words DStream中的一个RDD
		// 以此类推，直到生成最后一个，wordCounts RDD，作为wordCounts DStream中的一个RDD
		// 此时，就得到了，每秒钟发送过来的数据的单词统计
		// 但是，一定要注意，Spark Streaming的计算模型，就决定了，我们必须自己来进行中间缓存的控制
		// 比如写入redis等缓存
		// 它的计算模型跟Storm是完全不同的，storm是自己编写的一个一个的程序，运行在节点上，相当于一个
		// 一个的对象，可以自己在对象中控制缓存
		// 但是Spark本身是函数式编程的计算模型，所以，比如在words或pairs DStream中，没法在实例变量中
		// 进行缓存
		// 此时就只能将最后计算出的wordCounts中的一个一个的RDD，写入外部的缓存，或者持久化DB
		
		// 最后，每次计算完，都打印一下这一秒钟的单词计数情况
		// 并休眠5秒钟，以便于我们测试和观察
		
		Thread.sleep(5000);
		wordCounts.print();
		
		// 首先对JavaSteamingContext进行一下后续处理
		// 必须调用JavaStreamingContext的start()方法，整个Spark Streaming Application才会启动执行
		// 否则是不会执行的
		jssc.start();
		jssc.awaitTermination();
		jssc.close();
				
	}
}
