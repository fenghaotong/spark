package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object DataFrameCreate {
  def main(args: Array[String]){
    val conf = new SparkConf()
    .setAppName("DataFrameCreate")
    
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    
    val df = sqlContext.read.json("hdfs://spark1:9000/student.json")
    
    df.show();
    
  }
}