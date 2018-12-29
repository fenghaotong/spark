package cn.study.spark2

import org.apache.spark.sql.SparkSession

/*
 * untyped操作
 * sql语法
 * 	select
 * 	where
 * 	join	
 * 	groupBy
 * 	agg
 */

object UntypedOperation {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
    .builder()
    .appName("UntypedOperation")
    .master("local")
    .config("spark.sql.warehouse.dir", "C:\\Users\\htfeng\\Desktop\\spark-warehouse")
    .getOrCreate()
   
    import spark.implicits._
    import org.apache.spark.sql.functions._
    
    val employee = spark.read.json("C:\\Users\\htfeng\\Desktop\\employee.json")
    val department = spark.read.json("C:\\Users\\htfeng\\Desktop\\department.json")
    
    employee
        .where("age > 20")
        .join(department, $"depId" === $"id") 
        .groupBy(department("name"), employee("gender"))
        .agg(avg(employee("salary"))) 
        .show()
    
    employee
        .select($"name", $"depId", $"salary")
        .where("age > 20")
        .show()
    
    
  }
}