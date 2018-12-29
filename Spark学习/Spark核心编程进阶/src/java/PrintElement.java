package cn.spark.study.core.upgrade;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

public class PrintElement {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("PlusClosureVariable")
				.setMaster("local")
				.set("spark.default.parallelism", "2");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		List<Integer> numbers = Arrays.asList(1,2,3,4,5);
		JavaRDD<Integer> numbersRDD = sc.parallelize(numbers);
		
		
		numbersRDD.foreach(new VoidFunction<Integer>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void call(Integer num) throws Exception {
				System.out.println(num);
			}
		});
		
		
		sc.close();
	}
}
