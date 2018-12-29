package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

public class DataFrameOperation {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("DataFrameCreate");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlcontext = new SQLContext(sc);
		
		DataFrame df = sqlcontext.read().json("hdfs://spark1:9000/student.json");
		
		df.show();
		
		df.printSchema();
		
		df.select("name").show();
		
		df.select(df.col("name"), df.col("age").plus(1)).show();
		
		df.filter(df.col("age").gt(18)).show();
		
		df.groupBy("age").count().show();
	}
}
