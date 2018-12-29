package cn.spark.study.core

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object SortWordCount {
    def main(args: Array[String]) {
    val conf = new SparkConf()
        .setAppName("SortWordCount")
        .setMaster("local") 
    val sc = new SparkContext(conf)
    
    val lines = sc.textFile("C://Users//htfeng//Desktop//spark.txt", 1)
    
    val words = lines.flatMap{ line => line.split(" ") }
    val pairs = words.map{ word => (word, 1) }
    val wordCounts = pairs.reduceByKey(_ + _)
    
    val countWords = wordCounts.map{ wordCount => (wordCount._2, wordCount._1) }
    val sortCountWords = countWords.sortByKey(false)
    val sortWordCounts = sortCountWords.map{ sortCountWord => (sortCountWord._2, sortCountWord._1) }
    
    sortWordCounts.foreach(sortedWordCount => println(
        sortedWordCount._1 + " appear " + sortedWordCount._2 + " times."))
    }
}