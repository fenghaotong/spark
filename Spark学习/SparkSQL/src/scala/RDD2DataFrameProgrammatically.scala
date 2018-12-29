package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.StringType

object RDD2DataFrameProgrammatically extends App {
  val conf = new SparkConf().
  setMaster("local").
  setAppName("RDD2DataFrameProgrammatically")
  
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc);
  
  val studentRDD = sc.textFile("C://Users//htfeng//Desktop//students.txt", 1)
  .map {line => Row(line.split(",")(0).toInt, line.split(",")(1), line.split(",")(2).toInt) }
  
  val structType = StructType(Array(
      StructField("id", IntegerType, true),
      StructField("name", StringType, true),
      StructField("age", IntegerType, true)))  
      
  // 第三步，进行RDD到DataFrame的转换
  val studentDF = sqlContext.createDataFrame(studentRDD, structType) 
  
  // 继续正常使用
  studentDF.registerTempTable("students")  
  
  val teenagerDF = sqlContext.sql("select * from students where age<=18")  
  
  val teenagerRDD = teenagerDF.rdd.collect().foreach { row => println(row) }    
}