package cn.spark.study.core.upgrade.applog;

import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class AppLogSpark {
	public static void main(String[] args) throws Exception{
		// 创建Spark配置和上下文对象
		SparkConf conf = new SparkConf()
				.setAppName("AppLogSpark")  
				.setMaster("local"); 
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// 读取日志文件，并创建一个RDD
		// 使用SparkContext的textFile()方法，即可读取本地磁盘文件，或者是HDFS上的文件
		// 创建出来一个初始的RDD，其中包含了日志文件中的所有数据
		JavaRDD<String> accessLogRDD = sc.textFile(
				"C:\\Users\\htfeng\\Desktop\\BigData\\Spark学习\\Spark学习\\Spark核心编程进阶\\data\\access.log");   
		// 将RDD映射为key-value格式，为后面的reduceByKey聚合做准备
		JavaPairRDD<String, AccessLogInfo> accessLogPairRDD = mapAccessLogRDD2Pair(accessLogRDD);
		
		// 根据deviceID进行聚合操作
		// 获取每个deviceID的总上行流量、总下行流量、最早访问时间戳
		JavaPairRDD<String, AccessLogInfo> aggrAccessLogPairRDD = aggregateByDeviceID(accessLogPairRDD);
		
		// 将按deviceID聚合RDD的key映射为二次排序key，value映射为deviceID
		JavaPairRDD<AccessLogSortKey, String> accessLogSortRDD = mapRDDKey2SortKey(aggrAccessLogPairRDD);
		
		// 执行二次排序操作，按照上行流量、下行流量以及时间戳进行倒序排序
		JavaPairRDD<AccessLogSortKey, String> sortedAccessLogRDD = accessLogSortRDD.sortByKey(false);
		
		// 获取top10数据
		List<Tuple2<AccessLogSortKey, String>> top10DataList = sortedAccessLogRDD.take(10);
		
		for(Tuple2<AccessLogSortKey, String> data : top10DataList) {
			System.out.println(data._2 + ": " + data._1.getUpTraffic() + "," + data._1.getDownTraffic() + "," + data._1.getTimestamp());
		}
		
		// 关闭Spark上下文
		sc.close();
	}
	
	/**
	 * 将日志RDD映射为key-value的格式
	 * @param accessLogRDD 日志RDD
	 * @return key-value格式RDD
	 */
	private static JavaPairRDD<String, AccessLogInfo> mapAccessLogRDD2Pair(
			JavaRDD<String> accessLogRDD){
				return accessLogRDD.mapToPair(new PairFunction<String, String, AccessLogInfo>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Tuple2<String, AccessLogInfo> call(String accessLog) throws Exception {
						// 根据\t对日志进行切分
						String[] accessLogSplited = accessLog.split("\t");
						
						// 获取四个字段
						long timestamp = Long.valueOf(accessLogSplited[0]);
						String deviceID = accessLogSplited[1];
						long upTraffic = Long.valueOf(accessLogSplited[2]);
						long downTraffic = Long.valueOf(accessLogSplited[3]);
						
						// 将时间戳、上行流量、下行流量，封装为自定义的可序列化对象
						AccessLogInfo accessLogInfo = new AccessLogInfo(timestamp, upTraffic, downTraffic);
						
						return new Tuple2<String, AccessLogInfo>(deviceID, accessLogInfo);
					}
					
				});	
	}
	
	/**
	 * 根据deviceID进行聚合操作
	 * 计算出每个deviceID的总上行流量、总下行流量以及最早访问时间
	 * @param accessLogPairRDD 日志key-value格式RDD
	 * @return 按deviceID聚合RDD
	 */
	private static JavaPairRDD<String, AccessLogInfo> aggregateByDeviceID(
			JavaPairRDD<String, AccessLogInfo> accessLogPairRD){
		return accessLogPairRD.reduceByKey(new Function2<AccessLogInfo, AccessLogInfo, AccessLogInfo>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public AccessLogInfo call(AccessLogInfo accessLogInfo1, AccessLogInfo accessLogInfo2) throws Exception {
				long timestamp = accessLogInfo1.getTimestamp() < accessLogInfo2.getTimestamp()?
						accessLogInfo1.getTimestamp():accessLogInfo2.getTimestamp();
				long upTraffic = accessLogInfo1.getUpTraffic() + accessLogInfo2.getUpTraffic();
				long downTraffic = accessLogInfo1.getDownTraffic() + accessLogInfo2.getDownTraffic();
				
				AccessLogInfo accessLogInfo = new AccessLogInfo();
				accessLogInfo.setTimestamp(timestamp);
				accessLogInfo.setUpTraffic(upTraffic);
				accessLogInfo.setDownTraffic(downTraffic);
				
				return accessLogInfo;
			}
		});
	}
	/**
	 * 将RDD的key映射为二次排序key
	 * @param aggrAccessLogPairRDD 按deviceID聚合RDD
	 * @return 二次排序key RDD
	 */
	private static JavaPairRDD<AccessLogSortKey, String> mapRDDKey2SortKey(
			JavaPairRDD<String, AccessLogInfo> aggrAccessLogPairRDD){
				return aggrAccessLogPairRDD.mapToPair(
						new PairFunction<Tuple2<String,AccessLogInfo>, AccessLogSortKey, String>() {
							private static final long serialVersionUID = 1L;

							@Override
							public Tuple2<AccessLogSortKey, String> call(Tuple2<String, AccessLogInfo> tuple)
									throws Exception {
								String deviceID = tuple._1;
								AccessLogInfo accessLogInfo = tuple._2;
								
								// 将日志信息封装成二次排序key
								AccessLogSortKey accessLogSortKey = new AccessLogSortKey(
										accessLogInfo.getUpTraffic(),
										accessLogInfo.getDownTraffic(),
										accessLogInfo.getTimestamp());
								return new Tuple2<AccessLogSortKey, String>(accessLogSortKey, deviceID);
							}
							
						});
	}
	
	
	
}
