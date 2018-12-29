package cn.spark.study.core.upgrade;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class AggregateByKey {
	
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("AggregateByKey")
				.setMaster("local");       
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		JavaRDD<String> lines = sc.textFile(
				"C://Users//htfeng//Desktop//hello.txt",
				3); 
		
		JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public Iterable<String> call(String line) throws Exception {
				return Arrays.asList(line.split(" "));  
			}
			
		});
		
		JavaPairRDD<String, Integer> pairs = words.mapToPair(
				
				new PairFunction<String, String, Integer>() {

					private static final long serialVersionUID = 1L;
					
					@Override
					public Tuple2<String, Integer> call(String word) throws Exception {
						return new Tuple2<String, Integer>(word, 1);
					}
					
				});
		
		// aggregateByKey，分为三个参数
		// reduceByKey认为是aggregateByKey的简化版
		// aggregateByKey最重要的一点是，多提供了一个函数，Seq Function
		// 就是说自己可以控制如何对每个partition中的数据进行先聚合，类似于mapreduce中的，map-side combine
		// 然后才是对所有partition中的数据进行全局聚合
		
		// 第一个参数是，每个key的初始值
		// 第二个是个函数，Seq Function，如何进行shuffle map-side的本地聚合
		// 第三个是个函数，Combiner Function，如何进行shuffle reduce-side的全局聚合
		
		JavaPairRDD<String, Integer> wordCounts = pairs.aggregateByKey(
				0, 
				
				new Function2<Integer, Integer, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Integer call(Integer v1, Integer v2)
							throws Exception {
						return v1 + v2;
					}
					
				}, 
				
				new Function2<Integer, Integer, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Integer call(Integer v1, Integer v2)
							throws Exception {
						return v1 + v2;
					}
					
				});
		
		List<Tuple2<String, Integer>> wordCountList = wordCounts.collect();
		for(Tuple2<String, Integer> wordCount : wordCountList) {
			System.out.println(wordCount);  
		}
		
		sc.close();
	}
	
}
