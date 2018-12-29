package cn.spark.study.core.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

public class Repartition {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("Repartition")
				.setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);
	
		// repartition算子，用于任意将rdd的partition增多，或者减少
		// 与coalesce不同之处在于，coalesce仅仅能将rdd的partition变少
		// 但是repartition可以将rdd的partiton变多
		
		// 建议使用的场景
		// 一个很经典的场景，使用Spark SQL从hive中查询数据时
		// Spark SQL会根据hive对应的hdfs文件的block数量还决定加载出来的数据rdd有多少个partition
		// 这里的partition数量，是我们根本无法设置的
		
		// 有些时候，可能它自动设置的partition数量过于少了，导致我们后面的算子的运行特别慢
		// 此时就可以在Spark SQL加载hive数据到rdd中以后
		// 立即使用repartition算子，将rdd的partition数量变多
		
		// 案例
		// 公司要增加新部门
		// 但是人员还是这么多，所以我们只能使用repartition操作，增加部门
		// 将人员平均分配到更多的部门中去
		
		List<String> staffList = Arrays.asList("张三", "李四", "王二", "麻子",
				"赵六", "王五", "李大个", "王大妞", "小明", "小倩");  
		JavaRDD<String> staffRDD = sc.parallelize(staffList, 3); 
		
		JavaRDD<String> staffRDD2 = staffRDD.mapPartitionsWithIndex(
				new Function2<Integer, Iterator<String>, Iterator<String>>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Iterator<String> call(Integer index, Iterator<String> iterator) throws Exception {
						List<String> list = new ArrayList<String>();
						
						while(iterator.hasNext()) {
							String staff = iterator.next();
							list.add("部门[" + (index + 1) + "], " + staff);
						}
						return list.iterator();
					}
				}, true);
		for(String staffInfo : staffRDD2.collect()) {
			System.out.println(staffInfo);  
		}
		
		JavaRDD<String> staffRDD3 = staffRDD2.repartition(6);
		
		JavaRDD<String> staffRDD4 = staffRDD3.mapPartitionsWithIndex(
				
				new Function2<Integer, Iterator<String>, Iterator<String>>() {

					private static final long serialVersionUID = 1L;
		
					@Override
					public Iterator<String> call(Integer index, Iterator<String> iterator)
							throws Exception {
						List<String> list = new ArrayList<String>();
						
						while(iterator.hasNext()) {
							String staff = iterator.next();
							list.add("部门[" + (index + 1) + "], " + staff);
						}
						
						return list.iterator();
					}
					
				}, true);
		
		for(String staffInfo : staffRDD4.collect()) {
			System.out.println(staffInfo);  
		}
		
		sc.close();
	}
}
