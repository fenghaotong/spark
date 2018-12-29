package cn.spark.study.streaming;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import com.google.common.base.Optional;

import scala.Tuple2;

/**
 * 基于持久化机制的实时wordcount程序
 * @author Administrator
 *
 */
public class PersistWordCount {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setMaster("local[2]")
				.setAppName("PersistWordCount");  
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));

		jssc.checkpoint("hdfs://spark1:9000/wordcount_checkpoint");  
		
		JavaReceiverInputDStream<String> lines = jssc.socketTextStream("spark1", 9999);
		
		JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Iterable<String> call(String line) throws Exception {
				return Arrays.asList(line.split(" "));  
			}
			
		});
		
		JavaPairDStream<String, Integer> pairs = words.mapToPair(
				
				new PairFunction<String, String, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> call(String word)
							throws Exception {
						return new Tuple2<String, Integer>(word, 1);
					}
					
				});

		JavaPairDStream<String, Integer> wordCounts = pairs.updateStateByKey(
				
				new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Optional<Integer> call(List<Integer> values,
							Optional<Integer> state) throws Exception {
						Integer newValue = 0;
						
						if(state.isPresent()) {
							newValue = state.get();
						}

						for(Integer value : values) {
							newValue += value;
						}
						
						return Optional.of(newValue);  
					}
					
				});
		
		// 每次得到当前所有单词的统计次数之后，将其写入mysql存储，进行持久化，以便于后续的J2EE应用程序
		// 进行显示
		wordCounts.foreachRDD(new Function<JavaPairRDD<String,Integer>, Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call(JavaPairRDD<String, Integer> wordCountsRDD) throws Exception {
				// 调用RDD的foreachPartition()方法
				wordCountsRDD.foreachPartition(new VoidFunction<Iterator<Tuple2<String,Integer>>>() {

					private static final long serialVersionUID = 1L;

					@Override
					public void call(Iterator<Tuple2<String, Integer>> wordCounts) throws Exception {
						// 给每个partition，获取一个连接
						Connection conn = ConnectionPool.getConnection();
					
						// 遍历partition中的数据，使用一个连接，插入数据库
						Tuple2<String, Integer> wordCount = null;
						while(wordCounts.hasNext()) {
							wordCount = wordCounts.next();
							
							String sql = "insert into wordcount(word,count) "
									+ "values('" + wordCount._1 + "'," + wordCount._2 + ")";  
							
							Statement stmt = conn.createStatement();
							stmt.executeUpdate(sql);
						}
						
						// 用完以后，将连接还回去
						ConnectionPool.returnConnection(conn);
					}
				});
				
				return null;
			}
			
		});
		
		jssc.start();
		jssc.awaitTermination();
		jssc.close();
	}
	
}
