package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.LongType


/**
 * @author Administrator
 */
object JSONDataSource {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
        .setAppName("JSONDataSource")  
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    
    // 创建学生成绩DataFrame
    val studentScoresDF = sqlContext.read.json("hdfs://spark1:9000/spark-study/students.json")
    
    // 查询出分数大于80分的学生成绩信息，以及学生姓名
    studentScoresDF.registerTempTable("student_scores")
    val goodStudentScoresDF = sqlContext.sql("select name,score from student_scores where score>=80")
    val goodStudentNames = goodStudentScoresDF.rdd.map { row => row(0) }.collect()  
    
    // 创建学生基本信息DataFrame
    val studentInfoJSONs = Array("{\"name\":\"Leo\", \"age\":18}", 
        "{\"name\":\"Marry\", \"age\":17}",
        "{\"name\":\"Jack\", \"age\":19}")
    val studentInfoJSONsRDD = sc.parallelize(studentInfoJSONs, 3);
    val studentInfosDF = sqlContext.read.json(studentInfoJSONsRDD)  
    
    // 查询分数大于80分的学生的基本信息
    studentInfosDF.registerTempTable("student_infos")
    
    var sql = "select name,age from student_infos where name in ("
    for(i <- 0 until goodStudentNames.length) {
      sql += "'" + goodStudentNames(i) + "'"
      if(i < goodStudentNames.length - 1) {
        sql += ","
      }
    }
    sql += ")"  
    
    val goodStudentInfosDF = sqlContext.sql(sql)
    
    // 将分数大于80分的学生的成绩信息与基本信息进行join
    val goodStudentsRDD = 
        goodStudentScoresDF.rdd.map { row => (row.getAs[String]("name"), row.getAs[Long]("score")) }
            .join(goodStudentInfosDF.rdd.map { row => (row.getAs[String]("name"), row.getAs[Long]("age")) })  
  
    // 将rdd转换为dataframe
    val goodStudentRowsRDD = goodStudentsRDD.map(
        info => Row(info._1, info._2._1.toInt, info._2._2.toInt))  
            
    val structType = StructType(Array(
        StructField("name", StringType, true),
        StructField("score", IntegerType, true),
        StructField("age", IntegerType, true)))  
        
    val goodStudentsDF = sqlContext.createDataFrame(goodStudentRowsRDD, structType)  
    
    // 将dataframe中的数据保存到json中
    goodStudentsDF.write.format("json").save("hdfs://spark1:9000/spark-study/good-students-scala")  
  }
  
}