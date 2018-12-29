package cn.spark.study.streaming;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import scala.Tuple2;

public class KafkaWordCount {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setMaster("local[2]")
				.setAppName("HDFSWordCount");  
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
		// 使用KafkaUtils.createStream()方法，创建针对Kafka的输入数据流
		Map<String, Integer> topicThreadMap = new HashMap<String, Integer>();
		topicThreadMap.put("WordCount", 1);
		
		JavaPairReceiverInputDStream<String, String> lines = KafkaUtils.createStream(
				jssc, 
				"192.168.75.111:2181,192.168.75.112:2181,192.168.75.113:2181", 
				"DefaultConsumerGroup", 
				topicThreadMap);
		// 然后开发wordcount逻辑
		JavaDStream<String> words = lines.flatMap(
				new FlatMapFunction<Tuple2<String,String>, String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Iterable<String> call(Tuple2<String, String> tuple) throws Exception {
						// TODO Auto-generated method stub
						return Arrays.asList(tuple._2.split(" "));
					}
					
				});
		
		JavaPairDStream<String, Integer> pairs = words.mapToPair(
				new PairFunction<String, String, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> call(String word) throws Exception {
						// TODO Auto-generated method stub
						return new Tuple2<String, Integer>(word, 1);
					}
		});
		
		JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey(
				
				new Function2<Integer, Integer, Integer>() {
			
					private static final long serialVersionUID = 1L;

					@Override
					public Integer call(Integer v1, Integer v2) throws Exception {
						return v1 + v2;
					}
					
				});
		
		wordCounts.print();  
		
		jssc.start();
		jssc.awaitTermination();
		jssc.stop();
	}
}
