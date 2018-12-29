package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext


/**
 * 如果要用scala开发spark程序
 * 然后在其中，还要实现基于反射的RDD到DataFrame的转换，就必须得用object extends App的方式
 * 不能用def main()方法的方式，来运行程序，否则就会报no typetag for ...class的错误
 * @author Administrator
 */
object RDD2DataFrameReflection extends App {
  
  val conf = new SparkConf()
      .setMaster("local")  
      .setAppName("RDD2DataFrameReflection")  
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)
  
  // 在Scala中使用反射方式，进行RDD到DataFrame的转换，需要手动导入一个隐式转换
  import sqlContext.implicits._  
  
  case class Student(id: Int, name: String, age: Int)
  
  // 这里其实就是一个普通的，元素为case class的RDD
  // 直接对它使用toDF()方法，即可转换为DataFrame
  val studentDF = sc.textFile("C://Users//htfeng//Desktop//students.txt", 1)
      .map { line => line.split(",") }
      .map { arr => Student(arr(0).trim().toInt, arr(1), arr(2).trim().toInt) }
      .toDF()    
    
  studentDF.registerTempTable("students")  
  
  val teenagerDF = sqlContext.sql("select * from students where age<=18")  
  
  val teenagerRDD = teenagerDF.rdd
  
  // 在scala中，row中的数据的顺序，反而是按照我们期望的来排列的，这个跟java是不一样的哦
  teenagerRDD.map { row => Student(row(0).toString().toInt, row(1).toString(), row(2).toString().toInt) }
      .collect()
      .foreach { stu => println(stu.id + ":" + stu.name + ":" + stu.age) }  
      
  // 在scala中，对row的使用，比java中的row的使用，更加丰富
  // 在scala中，可以用row的getAs()方法，获取指定列名的列
  teenagerRDD.map { row => Student(row.getAs[Int]("id"), row.getAs[String]("name"), row.getAs[Int]("age")) }
      .collect()
      .foreach { stu => println(stu.id + ":" + stu.name + ":" + stu.age) }
  
  // 还可以通过row的getValuesMap()方法，获取指定几列的值，返回的是个map
  val studentRDD = teenagerRDD.map { row => {
      val map = row.getValuesMap[Any](Array("id", "name", "age")); 
      Student(map("id").toString().toInt, map("name").toString(), map("age").toString().toInt)    
    } 
  }  
  studentRDD.collect().foreach { stu => println(stu.id + ":" + stu.name + ":" + stu.age) }  
}