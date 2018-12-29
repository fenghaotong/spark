package cn.spark.study.core;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

/**
 * 对文本文件内的每个单词都统计出其出现的次数。
 * 按照每个单词出现次数的数量，降序排序。
 * @author htfeng
 *
 */
public class SortWordCount {
	public static void main(String[] args) {
		// 创建SparkConf和JavaSparkContext
		SparkConf conf = new SparkConf()
				.setAppName("SortWordCount")
				.setMaster("local"); 
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 创建lines RDD
		JavaRDD<String> lines = sc.textFile("C://Users//htfeng//Desktop//spark.txt");
		
		//执行我们之前做过的单词计数
		JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>(){
			
			private static final long serialVersionUID = 1L;

			@Override
			public Iterable<String> call(String t) throws Exception {
				// TODO Auto-generated method stub
				return Arrays.asList(t.split(" "));
			}
			
		});
		
		JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>(){

			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String, Integer> call(String t) throws Exception {
				// TODO Auto-generated method stub
				return new Tuple2<String, Integer>(t, 1);
			}
			
		});
		
		JavaPairRDD<String, Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>(){

			private static final long serialVersionUID = 1L;

			@Override
			public Integer call(Integer v1, Integer v2) throws Exception {
				// TODO Auto-generated method stub
				return v1 + v2;
			}
			
		});
		// 到这里为止，就得到了每个单词出现的次数
		// 但是，问题是，我们的新需求，是要按照每个单词出现次数的顺序，降序排序
		// wordCounts RDD内的元素是什么？应该是这种格式的吧：(hello, 3) (you, 2)
		// 我们需要将RDD转换成(3, hello) (2, you)的这种格式，才能根据单词出现次数进行排序把！
		
		// 进行key-value的反转映射
		JavaPairRDD<Integer, String> countWords = wordCounts.mapToPair(
				new PairFunction<Tuple2<String, Integer>, Integer, String>(){

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<Integer, String> call(Tuple2<String, Integer> t) throws Exception {
						// TODO Auto-generated method stub
						return new Tuple2<Integer, String>(t._2, t._1);
					}
					
				});
		
		// 按照Key进行排序
		JavaPairRDD<Integer, String> sortedCountWords = countWords.sortByKey(false);
		
		// 再此奖value-key进行反转映射
		JavaPairRDD<String, Integer> sortedWordCounts = sortedCountWords.mapToPair(
				new PairFunction<Tuple2<Integer, String>, String, Integer>(){

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> call(Tuple2<Integer, String> t) throws Exception {
						// TODO Auto-generated method stub
						return new Tuple2<String, Integer>(t._2, t._1);
					}
					
				});
		
		// dayin
		sortedWordCounts.foreach(new VoidFunction<Tuple2<String, Integer>>(){

			private static final long serialVersionUID = 1L;

			@Override
			public void call(Tuple2<String, Integer> t) throws Exception {
				// TODO Auto-generated method stub
				System.out.println(t._1 + " appears " + t._2 + " times");
			}
			
		});
		
		sc.close();
	}
}
