package cn.spark.study.streaming.upgrade;

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
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;

import scala.Tuple2;

/**
 * 基于Flume Poll方式的实时wordcount程序
 * @author Administrator
 *
 */
public class FlumePollWordCount {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setMaster("local[2]")
				.setAppName("FlumePollWordCount");  
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
		
		JavaReceiverInputDStream<SparkFlumeEvent> lines =
				FlumeUtils.createPollingStream(jssc, "192.168.75.101", 8888);  
		
		JavaDStream<String> words = lines.flatMap(
				
				new FlatMapFunction<SparkFlumeEvent, String>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Iterable<String> call(SparkFlumeEvent event) throws Exception {
						String line = new String(event.event().getBody().array());  
						return Arrays.asList(line.split(" "));   
					}
					
				});
		
		JavaPairDStream<String, Integer> pairs = words.mapToPair(
				
				new PairFunction<String, String, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> call(String word) throws Exception {
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
		jssc.close();
	}
	
}

