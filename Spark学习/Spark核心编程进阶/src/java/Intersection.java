package cn.spark.study.core.upgrade;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class Intersection {
	
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("Intersection")
				.setMaster("local");  
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// intersection算子
		// 获取两个rdd中，相同的数据
		
		// 有的公司内，有些人可能同时在做不同的项目，属于不同的项目组
		// 所以要针对代表两个项目组同事的rdd，取出其交集
		
		List<String> project1MemberList = Arrays.asList("张三", "李四", "王二", "麻子");  
		JavaRDD<String> project1MemberRDD = sc.parallelize(project1MemberList);
		
		List<String> project2MemberList = Arrays.asList("张三", "王五", "小明", "小倩");  
		JavaRDD<String> project2MemberRDD = sc.parallelize(project2MemberList);
		
		JavaRDD<String> projectIntersectionRDD = project1MemberRDD.intersection(project2MemberRDD);

		for(String member : projectIntersectionRDD.collect()) {
			System.out.println(member);  
		}
		
		sc.close();
	}
	
}
