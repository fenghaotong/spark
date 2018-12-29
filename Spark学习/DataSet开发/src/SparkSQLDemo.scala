package cn.study.spark2

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Row

/*
 * SparkSession、Dataframe、Dataset开发入门
 */

object SparkSQLDemo {
  // 定义一个case class
  // type强类型操作
  case class Person(name:String, age:Long)
  
  // 定义操作Hive数据的case class
  case class Record(key:Int, value:String)
    
  def main(args:Array[String]){
    val spark = SparkSession
    .builder()
    .appName("Spark SQL Example")
//    .master("local") 
//    // 必须设置
//    .config("spark.sql.warehouse.dir", 
//        "C:\\Users\\htfeng\\Desktop\\spark-warehouse")  
    .enableHiveSupport()
    .getOrCreate()
  
    import spark.implicits._
    import spark.sql
    
    // 从json读取文件，构造一个untype弱类型的dataframe
    // dataframe相当于Dataset[row]
    val df = spark.read.json("hdfs://192.168.75.11:9000/test_data/people.json")
    df.show()
    df.printSchema()   // 打印元数据
    df.select("name").show()    // select操作，典型的弱类型untype操作
    df.select($"name", $"age" + 1).show()   // 使用表达式，scala语法，要用$符号作为前缀
    df.filter($"age" > 21).show() // filter操作
    df.groupBy("age").count().show() // groupBy分组，在聚合
    
    // 基于dataframe创建临时表
    df.createOrReplaceTempView("people")
    // 使用SparkSession的sql函数
    val sqlDF = spark.sql("SELECT * FROM people")
    sqlDF.show()
    
    // 直接基于jvm object来构造dataset
    val caseClassDS = Seq(Person("Andy", 32)).toDS()
    caseClassDS.show()
    
    // 基于原始数据类型构造dataset
    val primitiveDS = Seq(1, 2, 3).toDS()
    primitiveDS.map(_ + 1).collect()
    
    // 基于已有的结构化数据文件，构造dataset
    val path = "hdfs://192.168.75.11:9000/test_data/people.json"
    val peopleDS = spark.read.json(path).as[Person]
    peopleDS.show()
    
    // 创建hive表
    sql("CREATE TABLE IF NOT EXISTS src (key INT, value STRING)")
    sql("LOAD DATA LOCAL INPATH '/usr/local/test_data/kv1.txt' INTO TABLE src")
    sql("SELECT * FROM src").show()
    sql("SELECT COUNT(*) FROM src").show()
    
    val sqlHiveDF = sql("SELECT key, value FROM src WHERE key < 10 ORDER BY key")
    val sqlHiveDS = sqlHiveDF.map {
      case Row (key: Int, value: String) => s"Key: $key, Value: $value"
    }
    sqlHiveDS.show()
    
    val recordsDF = spark.createDataFrame((1 to 100).map(i => Record(i, s"val_$i")))
    recordsDF.createOrReplaceTempView("records")
    sql("SELECT * FROM records r JOIN src s ON r.key = s.key").show()

  }
}