package cn.spark.study.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

/**
 * Action操作实战
 * @author htfeng
 *
 */
public class ActionOperation {
	public static void main(String[] args) {
		// reduce();
		// collect();
		// count();
		// take();
		saveAsTextFile();
		// countByKey();
	}
	
	private static void reduce() {
		// 创建SparkConf和JavaSparkContext
		SparkConf conf = new SparkConf()
				.setAppName("reduce")
				.setMaster("local");  
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 有一个集合，里面有1到10,10个数字，现在要对10个数字进行累加
		List<Integer> numberList = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		
		JavaRDD<Integer> numbers = sc.parallelize(numberList);
		
		// 使用reduce操作对集合中的数字进行累加
		// reduce操作的原理：
			// 首先将第一个和第二个元素，传入call()方法，进行计算，会获取一个结果，比如1 + 2 = 3
			// 接着将该结果与下一个元素传入call()方法，进行计算，比如3 + 3 = 6
			// 以此类推
		// 所以reduce操作的本质，就是聚合，将多个元素聚合成一个元素
		
		int sum = numbers.reduce(new Function2<Integer, Integer, Integer>(){
			private static final long serialVersionUID = 1L;

			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				// TODO Auto-generated method stub
				return v1 + v2;
			}
			
		});
		
		System.out.println(sum);
		
		sc.close();
	}
	
	private static void collect() {
		// 创建SparkConf和JavaSparkContext
		SparkConf conf = new SparkConf()
				.setAppName("collect")
				.setMaster("local");  
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 有一个集合，里面有1到10,10个数字，现在要对10个数字进行累加
		List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		JavaRDD<Integer> numbers = sc.parallelize(numberList);
		
		// 使用map操作将集合中所有数字乘以2
		JavaRDD<Integer> doubleNumbers = numbers.map(new Function<Integer, Integer>(){
			private static final long serialVersionUID = 1L;
		
			@Override
			public Integer call(Integer v1) throws Exception {
				// TODO Auto-generated method stub
				return v1 * 2;
			}
			
		});
		// 不用foreach action操作，在远程集群上遍历rdd中的元素
		// 而使用collect操作，将分布在远程集群上的doubleNumbers RDD的数据拉取到本地
		// 这种方式，一般不建议使用，因为如果rdd中的数据量比较大的话，比如超过1万条
			// 那么性能会比较差，因为要从远程走大量的网络传输，将数据获取到本地
			// 此外，除了性能差，还可能在rdd中数据量特别大的情况下，发生oom异常，内存溢出
		// 因此，通常，还是推荐使用foreach action操作，来对最终的rdd元素进行处理
		List<Integer> doubleNumberList = doubleNumbers.collect();
		for(Integer num : doubleNumberList) {
			System.out.println(num);  
		}
		
		// 关闭JavaSparkContext
		sc.close();
	}
	private static void count() {
		// 创建SparkConf和JavaSparkContext
		SparkConf conf = new SparkConf()
				.setAppName("count")
				.setMaster("local");  
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 有一个集合，里面有1到10,10个数字，现在要对10个数字进行累加
		List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		JavaRDD<Integer> numbers = sc.parallelize(numberList);
		
		// 对rdd使用count操作，统计它有多少个元素
		long count = numbers.count();
		System.out.println(count);  
		
		// 关闭JavaSparkContext
		sc.close();
	}
	
	private static void take() {
		// 创建SparkConf和JavaSparkContext
		SparkConf conf = new SparkConf()
				.setAppName("take")
				.setMaster("local");  
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 有一个集合，里面有1到10,10个数字，现在要对10个数字进行累加
		List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		JavaRDD<Integer> numbers = sc.parallelize(numberList);
		
		// 对rdd使用count操作，统计它有多少个元素
		// take操作，与collect类似，也是从远程集群上，获取rdd的数据
		// 但是collect是获取rdd的所有数据，take只是获取前n个数据
		List<Integer> top3Numbers = numbers.take(3);
		
		for(Integer num : top3Numbers) {
			System.out.println(num);  
		}
		
		// 关闭JavaSparkContext
		sc.close();
	}
	
	private static void saveAsTextFile() {
		// 创建SparkConf和JavaSparkContext
		SparkConf conf = new SparkConf()
				.setAppName("saveAsTextFile");  
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 有一个集合，里面有1到10,10个数字，现在要对10个数字进行累加
		List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		JavaRDD<Integer> numbers = sc.parallelize(numberList);
		
		// 使用map操作将集合中所有数字乘以2
		JavaRDD<Integer> doubleNumbers = numbers.map(
				
				new Function<Integer, Integer>() {

					private static final long serialVersionUID = 1L;
		
					@Override
					public Integer call(Integer v1) throws Exception {
						return v1 * 2;
					}
					
				});
		
		// 直接将rdd中的数据，保存在HFDS文件中
		// 但是要注意，我们这里只能指定文件夹，也就是目录
		// 那么实际上，会保存为目录中的/double_number.txt/part-00000文件
		doubleNumbers.saveAsTextFile("hdfs://spark1:9000/double_number.txt");   
		
		// 关闭JavaSparkContext
		sc.close();
	}
	
	@SuppressWarnings("unchecked")
	private static void countByKey() {
		// 创建SparkConf
		SparkConf conf = new SparkConf()
				.setAppName("countByKey")  
				.setMaster("local");
		// 创建JavaSparkContext
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 模拟集合
		List<Tuple2<String, String>> scoreList = Arrays.asList(
				new Tuple2<String, String>("class1", "leo"),
				new Tuple2<String, String>("class2", "jack"),
				new Tuple2<String, String>("class1", "marry"),
				new Tuple2<String, String>("class2", "tom"),
				new Tuple2<String, String>("class2", "david"));  
		
		// 并行化集合，创建JavaPairRDD
		JavaPairRDD<String, String> students = sc.parallelizePairs(scoreList);
		
		// 对rdd应用countByKey操作，统计每个班级的学生人数，也就是统计每个key对应的元素个数
		// 这就是countByKey的作用
		// countByKey返回的类型，直接就是Map<String, Object>
		Map<String, Object> studentCounts = students.countByKey();
		
		for(Map.Entry<String, Object> studentCount : studentCounts.entrySet()) {
			System.out.println(studentCount.getKey() + ": " + studentCount.getValue());  
		}
		
		// 关闭JavaSparkContext
		sc.close();
	}
	
}
