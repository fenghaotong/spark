package cn.spark.study.core

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object ActionOperation {
  def main(args: Array[String]) {
    // reduce()
    // collect()
    // count()
    // take()
    saveAsTextFile()
    //countByKey()
  }
  
  def reduce(){
    val conf = new SparkConf()
    .setAppName("reduce")
    .setMaster("local")  
    val sc = new SparkContext(conf)
    
    val numberArray = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val numbers = sc.parallelize(numberArray, 1)
    val sum = numbers.reduce(_ + _)
    
    println(sum)
  }
  
  def collect(){
        val conf = new SparkConf()
        .setAppName("collect")
        .setMaster("local")  
    val sc = new SparkContext(conf)
    
    val numberArray = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val numbers = sc.parallelize(numberArray, 1)
    val doubleNumbers = numbers.map{ num => num * 2 }
    val doubleNumberArray = doubleNumbers.collect()
    
    
    for(num <- doubleNumberArray){
      println(num)
    }
  }
  
  def count() {
    val conf = new SparkConf()
        .setAppName("count")
        .setMaster("local")  
    val sc = new SparkContext(conf)
    
    val numberArray = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val numbers = sc.parallelize(numberArray, 1)  
    val count = numbers.count()
    
    println(count)  
  }
  
  def take() {
    val conf = new SparkConf()
        .setAppName("take")
        .setMaster("local")  
    val sc = new SparkContext(conf)
    
    val numberArray = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val numbers = sc.parallelize(numberArray, 1)  
    
    val top3Numbers = numbers.take(3)
    
    for(num <- top3Numbers) {
      println(num)  
    }
  }
  
  def saveAsTextFile() {
    val conf = new SparkConf()
    .setAppName("saveAsTextFile")
    val sc = new SparkContext(conf)
    
    val numberArray = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val numbers = sc.parallelize(numberArray, 1)  
    val doubleNumbers = numbers.map { num => num * 2 }
    
    doubleNumbers.saveAsObjectFile("hdfs://spark1:9000/double2_number.txt")
    
  }
  
  def countByKey() {
    val conf = new SparkConf()
        .setAppName("countByKey")  
        .setMaster("local")  
    val sc = new SparkContext(conf)
    
    val studentList = Array(Tuple2("class1", "leo"), Tuple2("class2", "jack"),
        Tuple2("class1", "tom"), Tuple2("class2", "jen"), Tuple2("class2", "marry"))   
    val students = sc.parallelize(studentList, 1)  
    val studentCounts = students.countByKey()  
    
    println(studentCounts)  
  }
}