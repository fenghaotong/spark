package cn.spark.study.streaming;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

/**
 * 与Spark SQL整合使用，top3热门商品实时统计
 * @author Administrator
 *
 */

public class Top3HotProduct {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setMaster("local[2]")
				.setAppName("Top3HotProduct");  
		@SuppressWarnings("resource")
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
		// 首先看一下，输入日志的格式
		// leo iphone mobile_phone
		
		// 首先，获取输入数据流
		// 这里顺带提一句，之前没有讲过，就是说，我们的Spark Streaming的案例为什么都是基于socket的呢？
		// 因为方便啊。。。
		// 其实，企业里面，真正最常用的，都是基于Kafka这种数据源
		// 但是我觉得我们的练习，用socket也无妨，比较方便，而且一点也不影响学习
		// 因为不同的输入来源的，不同之处，只是在创建输入DStream的那一点点代码
		// 所以，核心是在于之后的Spark Streaming的实时计算
		// 所以只要我们掌握了各个案例和功能的使用
		// 在企业里，切换到Kafka，易如反掌把，因为我们之前都详细讲过，而且实验过，实战编码过，将Kafka作为
		// 数据源的两种方式了
		
		// 获取输入数据流
		JavaReceiverInputDStream<String> productClickLogsDStream = jssc.socketTextStream("spark1", 9999);
		
		// 然后，应该是做一个映射，将每个种类的每个商品，映射为(category_product, 1)的这种格式
		// 从而在后面可以使用window操作，对窗口中的这种格式的数据，进行reduceByKey操作
		// 从而统计出来，一个窗口中的每个种类的每个商品的，点击次数
		JavaPairDStream<String, Integer> categoryProductPairsDStream = productClickLogsDStream
				.mapToPair(new PairFunction<String, String, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, Integer> call(String productClickLog)
							throws Exception {
						String[] productClickLogSplited = productClickLog.split(" "); 
						return new Tuple2<String, Integer>(productClickLogSplited[2] + "_" + 
								productClickLogSplited[1], 1);
					}
					
				});
		
		// 然后执行window操作
		// 到这里，就可以做到，每隔10秒钟，对最近60秒的数据，执行reduceByKey操作
		// 计算出来这60秒内，每个种类的每个商品的点击次数
		JavaPairDStream<String, Integer> categoryProductCountsDStream = 
				categoryProductPairsDStream.reduceByKeyAndWindow(
						
						new Function2<Integer, Integer, Integer>() {

							private static final long serialVersionUID = 1L;
				
							@Override
							public Integer call(Integer v1, Integer v2) throws Exception {
								return v1 + v2;
							}
							
						}, Durations.seconds(60), Durations.seconds(10));  
		
		// 然后针对60秒内的每个种类的每个商品的点击次数
		// foreachRDD，在内部，使用Spark SQL执行top3热门商品的统计
		categoryProductCountsDStream.foreachRDD(new Function<JavaPairRDD<String,Integer>, Void>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Void call(JavaPairRDD<String, Integer> categoryProductCountsRDD) throws Exception {
				// 将该RDD，转换为JavaRDD<Row>的格式
				JavaRDD<Row> categoryProductCountRowRDD = categoryProductCountsRDD.map(
						
						new Function<Tuple2<String,Integer>, Row>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Row call(Tuple2<String, Integer> categoryProductCount)
									throws Exception {
								String category = categoryProductCount._1.split("_")[0];
								String product = categoryProductCount._1.split("_")[1];
								Integer count = categoryProductCount._2;
								return RowFactory.create(category, product, count);   
							}
							
						});
				
				// 然后，执行DataFrame转换
				List<StructField> structFields = new ArrayList<StructField>();
				structFields.add(DataTypes.createStructField("category", DataTypes.StringType, true)); 
				structFields.add(DataTypes.createStructField("product", DataTypes.StringType, true));  
				structFields.add(DataTypes.createStructField("click_count", DataTypes.IntegerType, true));  
				StructType structType = DataTypes.createStructType(structFields);
				
				HiveContext hiveContext = new HiveContext(categoryProductCountsRDD.context());
				
				DataFrame categoryProductCountDF = hiveContext.createDataFrame(
						categoryProductCountRowRDD, structType);
				
				// 将60秒内的每个种类的每个商品的点击次数的数据，注册为一个临时表
				categoryProductCountDF.registerTempTable("product_click_log");  
				
				// 执行SQL语句，针对临时表，统计出来每个种类下，点击次数排名前3的热门商品
				DataFrame top3ProductDF = hiveContext.sql(
						"SELECT category,product,click_count "
						+ "FROM ("
							+ "SELECT "
								+ "category,"
								+ "product,"
								+ "click_count,"
								+ "row_number() OVER (PARTITION BY category ORDER BY click_count DESC) rank "
							+ "FROM product_click_log"  
						+ ") tmp "
						+ "WHERE rank<=3");
				
				// 这里说明一下，其实在企业场景中，可以不是打印的
				// 案例说，应该将数据保存到redis缓存、或者是mysql db中
				// 然后，应该配合一个J2EE系统，进行数据的展示和查询、图形报表
				
				top3ProductDF.show();      
				
				return null;
			}
			
		});
		
		jssc.start();
		jssc.awaitTermination();
		jssc.close();
	}
}
