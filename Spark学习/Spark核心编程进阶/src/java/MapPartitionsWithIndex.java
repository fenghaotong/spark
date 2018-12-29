package cn.spark.study.core.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

public class MapPartitionsWithIndex {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("mapPartitionsWithIndex")
				.setMaster("local");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		List<String> studentNames = Arrays.asList("张三", "李四", "王二", "麻子");  
		JavaRDD<String> studentNamesRDD = sc.parallelize(studentNames, 2);
		
		// 这里，parallelize并行集合的时候，指定了numPartitions是2
		// 也就是说，四个同学，会被分成2个班
		// 但是spark自己判定怎么分班
		
		// 如果你要分班的话，就必须拿到班级号
		// mapPartitionsWithIndex这个算子来做，这个算子可以拿到每个partition的index
		// 也就可以作为我们的班级号
		
		JavaRDD<String> studentWithClassRDD = studentNamesRDD.mapPartitionsWithIndex(
				new Function2<Integer, Iterator<String>, Iterator<String>>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Iterator<String> call(Integer index, Iterator<String> iterator) throws Exception {
						List<String> studentWithClassList = new ArrayList<String>();
						
						while(iterator.hasNext()) {
							String studentName = iterator.next();
							String studentWithClass = studentName + "_" + (index + 1);
							studentWithClassList.add(studentWithClass);
						}
						return studentWithClassList.iterator();
					}
				}, true);
		
		for(String studentWithClass : studentWithClassRDD.collect()) {
			System.out.println(studentWithClass);
		}
		
		sc.close();
	}
}
