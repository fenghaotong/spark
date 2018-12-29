package cn.spark.study.sql;

import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

public class ParquetLoadData {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("GenericLoadSave");
		
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		
		
		DataFrame usersDF = sqlContext.read().parquet("hdfs://spark1:9000/users.parquet");
		
		usersDF.registerTempTable("users");
		
		DataFrame userNamesDF = sqlContext.sql("select name from users");
		
		List<String> userNames = userNamesDF.javaRDD().map(new Function<Row, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String call(Row row) throws Exception {
				// TODO Auto-generated method stub
				return "Name: " + row.getString(0);
			}
		}).collect();
		
		for(String userName : userNames) {
			System.out.println(userName);
		}
	}

}
