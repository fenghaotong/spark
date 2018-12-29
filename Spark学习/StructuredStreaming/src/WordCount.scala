package cn.study.spark2

import org.apache.spark.SparkConf
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.sql.SparkSession

/**
 * @author Administrator
 */
object WordCount {
  
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
    .builder()
    .appName("StructuredNetworkWordCount")
    .getOrCreate()
    
    import spark.implicits._
    
    val lines = spark.readStream
    .format("socket")
    .option("host", "localhost")
    .option("port", 9999)
    .load()
    
    val words = lines.as[String].flatMap(_.split(" "))
    
    val wordCounts = words.groupBy("value").count()
    
    val query = wordCounts.writeStream
    .outputMode("complete")
    .format("console")
    .start()
    
    query.awaitTermination()

  }
  
}