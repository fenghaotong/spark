package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

public class GenericLoadSave {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("GenericLoadSave");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		
		DataFrame usersDF = sqlContext.read().load("hdfs://spark1:9000/users.parquet");
		
		usersDF.select("name","favorite_color").write()
		.save("hdfs://spark1:9000/namesAndFavColors.parquet"); 
		
	}
}
