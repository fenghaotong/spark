package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

public class DataFrameCreate {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("DataFrameCreate");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlcontext = new SQLContext(sc);
		
		DataFrame df = sqlcontext.read().json("hdfs://spark1:9000/students.json");
		
		df.show();
	}
}
