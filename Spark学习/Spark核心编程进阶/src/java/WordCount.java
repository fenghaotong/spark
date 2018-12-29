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


public class WordCount {
	
	public static void main(String[] args) {
		
		SparkConf conf = new SparkConf()
				.setAppName("WordCount");
//				.setMaster("local");  
		
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		// String file = "C://Users//htfeng//Desktop//spark.txt";
		String file = null;
		if(args != null && args.length > 0) {
			System.out.println("======================接收到了参数：" + args[0] + "======================");
			file = args[0];
		}else {
			file = "hdfs://192.168.75.101:9000/hello.txt";
		}
		
	
		JavaRDD<String> lines = sc.textFile(file);
	
		
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
		
		
		JavaPairRDD<String, Integer> wordCounts = pairs.reduceByKey(
				
				new Function2<Integer, Integer, Integer>() {
					
					private static final long serialVersionUID = 1L;
		
					@Override
					public Integer call(Integer v1, Integer v2) throws Exception {
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
