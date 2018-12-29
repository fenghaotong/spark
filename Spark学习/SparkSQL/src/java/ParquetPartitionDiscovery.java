package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

public class ParquetPartitionDiscovery {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("ParquetPartitionDiscovery");  
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		
		DataFrame usersDF = sqlContext.read().parquet(
				"hdfs://spark1:9000/spark-study/users/gender=male/country=US/users.parquet");
		usersDF.printSchema();
		usersDF.show();
	}
}
