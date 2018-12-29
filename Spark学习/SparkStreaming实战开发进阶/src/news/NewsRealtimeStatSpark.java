package cn.spark.study.streaming.upgrade.news;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import kafka.serializer.StringDecoder;
import scala.Tuple2;


public class NewsRealtimeStatSpark {
	public static void main(String[] args) throws Exception{
		SparkConf conf = new SparkConf()
				.setMaster("local[2]")
				.setAppName("NewsRealtimeStatSpark");
		
		JavaStreamingContext jssc = new JavaStreamingContext(
				conf, Durations.seconds(5));  
		
		// 创建输入DStream
		Map<String, String> kafkaParams = new HashMap<String, String>();
		kafkaParams.put("metadata.broker.list", 
				"192.168.75.101:9092,192.168.75.102:9092");
		
		Set<String> topics = new HashSet<String>();
		topics.add("news-access");  
		
		JavaPairInputDStream<String, String> lines = KafkaUtils.createDirectStream(
				jssc, 
				String.class, 
				String.class, 
				StringDecoder.class, 
				StringDecoder.class, 
				kafkaParams, 
				topics);
		
		// 过滤访问日志
		JavaPairDStream<String, String> accessDStream = lines.filter(
				new Function<Tuple2<String,String>, Boolean>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Boolean call(Tuple2<String, String> tuple) throws Exception {
						String log = tuple._2;
						String[] logSplited = log.split(" ");
						
						String action = logSplited[5];
						if("view".equals(action)) {
							return true;
						}else {
							return false;
						}
					}
				});
		
		// 统计第一个指标：实时页面pv
		calculatePagePv(accessDStream);  
		// 统计第二个指标：实时页面uv
		calculatePageUv(accessDStream); 
		// 统计第三个指标：实时注册用户数
		calculateRegisterCount(lines);  
		// 统计第四个指标：实时用户跳出数
		calculateUserJumpCount(accessDStream);
		// 统计第五个指标：实时版块pv
		calcualteSectionPv(accessDStream); 
		
		jssc.start();
		jssc.awaitTermination();
		jssc.stop();
	}
	
	/**
	 * 计算页面pv
	 * @param accessDStream
	 */
	private static void calculatePagePv(
			JavaPairDStream<String, String> accessDStream) {
		JavaPairDStream<Long, Long> pageidDStream = accessDStream.mapToPair(
			new PairFunction<Tuple2<String, String>, Long, Long>() {
				private static final long serialVersionUID = 1L;
	
				@Override
				public Tuple2<Long, Long> call(Tuple2<String, String> tuple) throws Exception {
					String log = tuple._2;
					String[] logSplited = log.split(" "); 
					
					Long pageid = Long.valueOf(logSplited[3]);
					return new Tuple2<Long, Long>(pageid, 1L);
				}
		});
		
		JavaPairDStream<Long, Long> pagePvDStream = pageidDStream.reduceByKey(
				
				new Function2<Long, Long, Long>() {
			
					private static final long serialVersionUID = 1L;
		
					@Override
					public Long call(Long v1, Long v2) throws Exception {
						return v1 + v2;
					}
					
				});
		pagePvDStream.print();
		
		// 在计算出每10秒钟的页面pv之后，其实在真实项目中，应该持久化
		// 到mysql，或redis中，对每个页面的pv进行累加
		// javaee系统，就可以从mysql或redis中，读取page pv实时变化的数据，以及曲线图
	}
	
	/**
	 * 计算页面uv
	 * @param <U>
	 * @param accessDStream
	 */
	
	private static <u> void calculatePageUv(
			JavaPairDStream<String, String> accessDStream) {
		JavaDStream<String> pageidUseridDStream = accessDStream.map(
				new Function<Tuple2<String,String>, String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String call(Tuple2<String, String> tuple) throws Exception {
						String log = tuple._2;
						String[] logSplited = log.split(" ");
						
						Long pageid = Long.valueOf(logSplited[3]);
						Long userid = Long.valueOf("null".equalsIgnoreCase(logSplited[2]) ? "-1":logSplited[2]);
						
						return pageid + "_" + userid;
					}
				});
		JavaDStream<String> distinctPageidUseridDStream = pageidUseridDStream.transform(
				new Function<JavaRDD<String>, JavaRDD<String>>() {

					private static final long serialVersionUID = 1L;

					@Override
					public JavaRDD<String> call(JavaRDD<String> rdd) throws Exception {
						// TODO Auto-generated method stub
						return rdd.distinct();
					}
					
				});
		
		JavaPairDStream<Long, Long> pageidDStream = distinctPageidUseridDStream.mapToPair(
				new PairFunction<String, Long, Long>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<Long, Long> call(String str) throws Exception {
						String[] splited = str.split("_");
						Long pageid = Long.valueOf(splited[0]);
						
						return new Tuple2<Long, Long>(pageid, 1L);
					}
				});
		
		JavaPairDStream<Long, Long> pageUvDStream = pageidDStream.reduceByKey(
				
				new Function2<Long, Long, Long>() {

					private static final long serialVersionUID = 1L;
		
					@Override
					public Long call(Long v1, Long v2) throws Exception {
						return v1 + v2;
					}
					
				});
		
		pageUvDStream.print();
		
	}
	
	/**
	 * 计算实时注册用户数
	 * @param lines
	 */
	private static void calculateRegisterCount(JavaPairInputDStream<String, String> lines) {
		JavaPairDStream<String, String> registerDStream = lines.filter(
				new Function<Tuple2<String,String>, Boolean>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Boolean call(Tuple2<String, String> tuple) throws Exception {
						String log = tuple._2;
						String[] logSplited = log.split(" ");  
						
						String action = logSplited[5];
						if("register".equals(action)) {
							return true;
						} else {
							return false;
						}
					}
				});
		
		JavaDStream<Long> registerCountDStream = registerDStream.count();
		
		registerCountDStream.print();
		
		// 每次统计完一个最近10秒的数据之后，不是打印出来
		// 去存储（mysql、redis、hbase），选用哪一种主要看你的公司提供的环境，以及你的看实时报表的用户以及并发数量，包括你的数据量
		// 如果是一般的展示效果，就选用mysql就可以
		// 如果是需要超高并发的展示，比如QPS 1w来看实时报表，那么建议用redis、memcached
		// 如果是数据量特别大，建议用hbase
		
		// 每次从存储中，查询注册数量，最近一次插入的记录，比如上一次是10秒前
		// 然后将当前记录与上一次的记录累加，然后往存储中插入一条新记录，就是最新的一条数据
		// 然后javaee系统在展示的时候，可以比如查看最近半小时内的注册用户数量变化的曲线图
		// 查看一周内，每天的注册用户数量的变化曲线图（每天就取最后一条数据，就是每天的最终数据）
	}
	
	/**
	 * 计算用户跳出数量
	 * @param accessDStream
	 */
	private static void calculateUserJumpCount(JavaPairDStream<String, String> accessDStream) {
		JavaPairDStream<Long, Long> useridDStream = accessDStream.mapToPair(
				new PairFunction<Tuple2<String,String>, Long, Long>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<Long, Long> call(Tuple2<String, String> tuple) throws Exception {
						String log = tuple._2;
						String[] logSplited = log.split(" ");  
						Long userid = Long.valueOf("null".equalsIgnoreCase(logSplited[2]) ? "-1" : logSplited[2]);    
						return new Tuple2<Long, Long>(userid, 1L);   
					}
				});
		JavaPairDStream<Long, Long> useridCountDStream = useridDStream.reduceByKey(
				
				new Function2<Long, Long, Long>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Long call(Long v1, Long v2) throws Exception {
						return v1 + v2;
					}
					
				});
		JavaPairDStream<Long, Long> jumpUserDStream = useridCountDStream.filter(
				
				new Function<Tuple2<Long,Long>, Boolean>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Boolean call(Tuple2<Long, Long> tuple) throws Exception {
						if(tuple._2 == 1) {
							return true;
						} else {
							return false;
						}
					}
					
				});
		
		JavaDStream<Long> jumpUserCountDStream = jumpUserDStream.count();
		jumpUserCountDStream.print();  
	}
	
	/**
	 * 版块实时pv
	 * @param accessDStream
	 */
	private static void calcualteSectionPv(JavaPairDStream<String, String> accessDStream) {
		JavaPairDStream<String, Long> sectionDStream = accessDStream.mapToPair(
				
				new PairFunction<Tuple2<String,String>, String, Long>() {

					private static final long serialVersionUID = 1L;
		
					@Override
					public Tuple2<String, Long> call(Tuple2<String, String> tuple)
							throws Exception {
						String log = tuple._2;
						String[] logSplited = log.split(" ");  
						
						String section = logSplited[4];
						
						return new Tuple2<String, Long>(section, 1L);  
					}
					
				});
		
		JavaPairDStream<String, Long> sectionPvDStream = sectionDStream.reduceByKey(
				
				new Function2<Long, Long, Long>() {

					private static final long serialVersionUID = 1L;
		
					@Override
					public Long call(Long v1, Long v2) throws Exception {
						return v1 + v2;
					}
					
				});
		
		sectionPvDStream.print();
	}
	
}
