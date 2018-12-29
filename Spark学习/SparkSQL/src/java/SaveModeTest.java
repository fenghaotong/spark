package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;

public class SaveModeTest {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("GenericLoadSave");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		
		DataFrame peopleDF = sqlContext.read().format("json")
				.load("hdfs://spark1:9000/people.json");
		peopleDF.save("hdfs://spark1:9000/people_savemode_test", "json", SaveMode.Append);
		
		
	}
}
