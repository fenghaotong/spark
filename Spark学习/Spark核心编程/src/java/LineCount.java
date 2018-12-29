package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

/**
 * 统计每行出现的次数
 * @author htfeng
 *
 */

public class LineCount {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("LineCount")
				.setMaster("local");
		// 创建JavaSparkContext
		JavaSparkContext sc = new JavaSparkContext(conf);
				
		//  使用SparkContext以及其子类的textFile()方法，针对本地文件创建RDD
		JavaRDD<String> lines = sc.textFile("C://Users//htfeng//Desktop//hello.txt");
		
		JavaPairRDD<String, Integer> pairs = lines.mapToPair(new PairFunction<String, String, Integer>(){
			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String, Integer> call(String t) throws Exception {
				// TODO Auto-generated method stub
				return new Tuple2<String, Integer>(t, 1);
			}
		});
		
		// 对pairRDD执行reduceByKey算子，统计每一行出现的总次数
		JavaPairRDD<String, Integer> lineCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>(){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				// TODO Auto-generated method stub
				return v1 + v2;
			}
			
		});
		
		// 执行action操作，foreach，打印每一行出现的次数
		lineCounts.foreach(new VoidFunction<Tuple2<String, Integer>>(){
			private static final long serialVersionUID = 1L;
			@Override
			public void call(Tuple2<String, Integer> t) throws Exception {
				// TODO Auto-generated method stub
				System.out.println(t._1 + "appears " + t._2 + " times.");
			}
			
		});
		
		sc.close();
	}
}
