package cn.spark.study.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.hive.HiveContext


object HiveDataSource {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
        .setAppName("HiveDataSource");
    val sc = new SparkContext(conf);
    val hiveContext = new HiveContext(sc);
    
    hiveContext.sql("DROP TABLE IF EXISTS student_infos");
    hiveContext.sql("CREATE TABLE IF NOT EXISTS student_infos (name STRING, age INT)");
    hiveContext.sql("LOAD DATA "
        + "LOCAL INPATH '/usr/local/spark-study/resources/student_infos.txt' "
        + "INTO TABLE student_infos");
    
    hiveContext.sql("DROP TABLE IF EXISTS student_scores"); 
    hiveContext.sql("CREATE TABLE IF NOT EXISTS student_scores (name STRING, score INT)");  
    hiveContext.sql("LOAD DATA "
        + "LOCAL INPATH '/usr/local/spark-study/resources/student_scores.txt' "
        + "INTO TABLE student_scores");
    
    val goodStudentsDF = hiveContext.sql("SELECT si.name, si.age, ss.score "
        + "FROM student_infos si "
        + "JOIN student_scores ss ON si.name=ss.name "
        + "WHERE ss.score>=80");
    
    hiveContext.sql("DROP TABLE IF EXISTS good_student_infos");  
    goodStudentsDF.saveAsTable("good_student_infos");  
    
    val goodStudentRows = hiveContext.table("good_student_infos").collect();  
    for(goodStudentRow <- goodStudentRows) {
      println(goodStudentRow);  
    }
  }
  
}