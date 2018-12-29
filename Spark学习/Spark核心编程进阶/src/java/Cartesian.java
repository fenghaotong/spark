package cn.spark.study.core.upgrade;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

public class Cartesian {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("Cartesian")
				.setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// cartesian，中文名，笛卡尔乘积
		// 比如说两个RDD，分别有10条数据，用了cartesian算子以后
		// 两个RDD的每一条数据都会和另外一个RDD的每一条数据执行一次join
		// 最终组成了一个笛卡尔乘积
		
		// 小案例
		// 比如说，现在5件衣服，5条裤子，分别属于两个RDD
		// 就是说，需要对每件衣服都和每天裤子做一次join，尝试进行服装搭配
		
		List<String> clothes = Arrays.asList("夹克", "T恤", "皮衣", "风衣");  
		JavaRDD<String> clothesRDD = sc.parallelize(clothes);
		
		List<String> trousers = Arrays.asList("皮裤", "运动裤", "牛仔裤", "休闲裤");  
		JavaRDD<String> trousersRDD = sc.parallelize(trousers);
		
		JavaPairRDD<String, String> pairsRDD = clothesRDD.cartesian(trousersRDD);
		for(Tuple2<String, String> pair : pairsRDD.collect()) {
			System.out.println(pair);  
		}
		
		sc.close();
	}
}
