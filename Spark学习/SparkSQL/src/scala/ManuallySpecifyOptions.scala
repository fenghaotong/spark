package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object ManuallySpecifyOptions {
  def main(args: Array[String]){
    val conf = new SparkConf()
    .setAppName("ManuallySpecifyOptions")
    
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc);
    
    val peopleDF = sqlContext.read.format("json").load("hdfs://spark1:9000/people.json")
    peopleDF.select("name").write.format("paruet").save("hdfs://spark1:9000/people.json")
  }
}