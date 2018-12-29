package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object GenericLoadSave {
  def main(args: Array[String]){
    val conf = new SparkConf()
    .setAppName("GenericLoadSave")
    
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    
    val usersDF = sqlContext.read.load("hdfs://spark1:9000/users.parquet")
    usersDF.write.save("hdfs://spark1:9000/namesAndFavColors_scala")  
  }
}