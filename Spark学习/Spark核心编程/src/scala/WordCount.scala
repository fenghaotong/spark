
package cn.spark.study.core

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext


object WordCount {
  
  def main(args: Array[String]) {
    val conf = new SparkConf()
        .setAppName("WordCount");
    val sc = new SparkContext(conf)
  
    val lines = sc.textFile("hdfs://spark1:9000/spark.txt", 1); 
    val words = lines.flatMap { line => line.split(" ") }   
    val pairs = words.map { word => (word, 1) }   
    val wordCounts = pairs.reduceByKey { _ + _ }
    
    wordCounts.foreach(wordCount => println(wordCount._1 + " appeared " + wordCount._2 + " times."))  
  }
  
}


