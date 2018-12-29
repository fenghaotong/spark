package cn.spark.study.core.upgrade;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class Union {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("Union") 
				.setMaster("local");  
		JavaSparkContext sc = new JavaSparkContext(conf);
	
		// union算子
		// 将两个RDD的数据，合并为一个RDD
		
		List<String> department1StaffList = Arrays.asList("张三", "李四", "王二", "麻子");  
		JavaRDD<String> department1StaffRDD = sc.parallelize(department1StaffList);
		
		List<String> department2StaffList = Arrays.asList("赵六", "王五", "小明", "小倩");  
		JavaRDD<String> department2StaffRDD = sc.parallelize(department2StaffList);
		
		JavaRDD<String> departmentStaffRDD = department1StaffRDD.union(department2StaffRDD);
		
		for(String staff : departmentStaffRDD.collect()) {
			System.out.println(staff);
		}
		
		sc.close();
	}
}
