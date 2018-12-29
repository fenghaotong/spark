package cn.spark.study.core

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object BroadcastVariable {
  def main(args: Array[String]){
    val conf = new SparkConf()
    .setAppName("BroadcastVariable")
    .setMaster("local")
    
    val sc = new SparkContext(conf)
    
    val factor = 3
    val factorBroadcast = sc.broadcast(factor)
    
    val numberArray = Array(1,2,3,4,5)
    val numbers = sc.parallelize(numberArray, 1)
    val multipleNumbers = numbers.map{ number => number * factorBroadcast.value}
    
    multipleNumbers.foreach{ num => println(num) }
  }
}