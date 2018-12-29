package cn.spark.study.core.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

public class MapPartitions {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("MapPartitions")
				.setMaster("local");  
		JavaSparkContext sc = new JavaSparkContext(conf);
	
		// 准备一下模拟数据
		List<String> studentNames = Arrays.asList("张三", "李四", "王二", "麻子");  
		JavaRDD<String> studentNamesRDD = sc.parallelize(studentNames, 2);
		
		final Map<String, Double> studentScoreMap = new HashMap<String, Double>();
		studentScoreMap.put("张三", 278.5);  
		studentScoreMap.put("李四", 290.0);  
		studentScoreMap.put("王二", 301.0);  
		studentScoreMap.put("麻子", 205.0);  
		
		// mapPartitions
		// 类似map，不同之处在于，map算子，一次就处理一个partition中的一条数据
		// mapPartitions算子，一次处理一个partition中所有的数据
		
		// 推荐的使用场景
		// 如果你的RDD的数据量不是特别大，那么建议采用mapPartitions算子替代map算子，可以加快处理速度
		// 但是如果你的RDD的数据量特别大，比如说10亿，不建议用mapPartitions，可能会内存溢出
		JavaRDD<Double> studentScoresRDD = studentNamesRDD.mapPartitions(
				new FlatMapFunction<Iterator<String>, Double>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Iterable<Double> call(Iterator<String> iterator) throws Exception {
						// 因为算子一次处理一个partition的所有数据
						// call函数接收的参数，是iterator类型，代表了partition中所有数据的迭代器
						// 返回的是一个iterable类型，代表了返回多条记录，通常使用List类型
						List<Double> studentScoreList = new ArrayList<Double>();
						
						while(iterator.hasNext()) {
							String studentName = iterator.next();
							Double studentScore = studentScoreMap.get(studentName);
							studentScoreList.add(studentScore);
						}
						
						return studentScoreList;
					}
				});
		for(Double studentScore: studentScoresRDD.collect()) {
			System.out.println(studentScore);  
		}
		
		sc.close();
	}
}
