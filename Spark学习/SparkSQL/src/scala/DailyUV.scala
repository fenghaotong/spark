package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.functions._

object DailyUV {
  def main(args: Array[String]){
   val conf = new SparkConf()
    .setMaster("local")  
    .setAppName("DailyUV")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
   
      // 这里着重说明一下！！！
    // 要使用Spark SQL的内置函数，就必须在这里导入SQLContext下的隐式转换
   import sqlContext.implicits._
   
   // 构造用户访问日志数据，并创建DataFrame
    
   // 模拟用户访问日志，日志用逗号隔开，第一列是日期，第二列是用户id
   val userAccessLog = Array(
        "2015-10-01,1122",
        "2015-10-01,1122",
        "2015-10-01,1123",
        "2015-10-01,1124",
        "2015-10-01,1124",
        "2015-10-02,1122",
        "2015-10-02,1121",
        "2015-10-02,1123",
        "2015-10-02,1123");
    val userAccessLogRDD = sc.parallelize(userAccessLog, 5)
    
    // 将模拟出来的用户访问日志RDD，转换为DataFrame
    // 首先，将普通的RDD，转换为元素为Row的RDD
    val userAccessLogRowRDD = userAccessLogRDD
    .map{ log => Row(log.split(",")(0), log.split(",")(1).toInt)}
    // 构造DataFrame的元数据
    val structType = StructType(Array(
        StructField("date", StringType, true),
        StructField("userid", IntegerType, true)))  
    // 使用SQLContext创建DataFrame
    val userAccessLogRowDF = sqlContext.createDataFrame(userAccessLogRowRDD, structType) 
    
    // 这里讲解一下uv的基本含义和业务
    // 每天都有很多用户来访问，但是每个用户可能每天都会访问很多次
    // 所以，uv，指的是，对用户进行去重以后的访问总数
    
    // 这里，正式开始使用Spark 1.5.x版本提供的最新特性，内置函数，countDistinct
    // 讲解一下聚合函数的用法
    // 首先，对DataFrame调用groupBy()方法，对某一列进行分组
    // 然后，调用agg()方法 ，第一个参数，必须，必须，传入之前在groupBy()方法中出现的字段
    // 第二个参数，传入countDistinct、sum、first等，Spark提供的内置函数
    // 内置函数中，传入的参数，也是用单引号作为前缀的，其他的字段
    userAccessLogRowDF.groupBy("date")
    .agg('date, countDistinct('userid))
    .map { row => Row(row(1), row(2)) }
    .collect()
    .foreach(println)
  }
}