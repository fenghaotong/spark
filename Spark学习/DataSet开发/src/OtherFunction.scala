package cn.study.spark2

import org.apache.spark.sql.SparkSession

object OtherFunction {
  
  def main(args: Array[String]) {
    val spark = SparkSession
        .builder()
        .appName("OtherFunction") 
        .master("local") 
        .config("spark.sql.warehouse.dir", "C:\\Users\\htfeng\\Desktop\\spark-warehouse")
        .getOrCreate()
    
    import spark.implicits._
    import org.apache.spark.sql.functions._
    
    val employee = spark.read.json("C:\\Users\\htfeng\\Desktop\\employee.json")
    val department = spark.read.json("C:\\Users\\htfeng\\Desktop\\department.json")
    
    // 日期函数：current_date、current_timestamp
    // 数学函数：round
    // 随机函数：rand
    // 字符串函数：concat、concat_ws
    // 自定义udf和udaf函数
    
    // http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.functions$
    
    employee
        .select(employee("name"), current_date(), current_timestamp(), rand(), round(employee("salary"), 2), concat(employee("gender"), employee("age")), concat_ws("|", employee("gender"), employee("age")))  
        .show()  
  }
  
}