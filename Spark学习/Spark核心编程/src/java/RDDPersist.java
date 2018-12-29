package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * RDD持久化
 * @author htfeng
 *
 */
public class RDDPersist {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("Persist")
				.setMaster("local"); 
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// cache()或者persist()的使用，是有规则的
		// 必须在transformation或者textFile等创建了一个RDD之后，直接连续调用cache()或persist()才可以
		// 如果你先创建一个RDD，然后单独另起一行执行cache()或persist()方法，是没有用的
		// 而且，会报错，大量的文件会丢失
		JavaRDD<String> lines = sc.textFile("C://Users//htfeng//Desktop//spark.txt").cache();
		
		long beginTime = System.currentTimeMillis();	
		long count = lines.count();
		System.out.println(count);
		long endTime = System.currentTimeMillis();
		System.out.println("cost " + (endTime - beginTime) + " milliseconds.");
		
		beginTime = System.currentTimeMillis();
		count = lines.count();
		System.out.println(count);
		endTime = System.currentTimeMillis();
		System.out.println("cost " + (endTime - beginTime) + " milliseconds.");
		
		sc.close();
	}
}
