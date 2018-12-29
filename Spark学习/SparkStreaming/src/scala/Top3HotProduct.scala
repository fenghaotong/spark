package cn.spark.study.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.hive.HiveContext

/**
 * @author Administrator
 */
object Top3HotProduct {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
        .setMaster("local[2]")  
        .setAppName("Top3HotProduct")
    val ssc = new StreamingContext(conf, Seconds(1))
    
    val productClickLogsDStream = ssc.socketTextStream("spark1", 9999)  
    val categoryProductPairsDStream = productClickLogsDStream
        .map { productClickLog => (productClickLog.split(" ")(2) + "_" + productClickLog.split(" ")(1), 1)}
    val categoryProductCountsDStream = categoryProductPairsDStream.reduceByKeyAndWindow(
        (v1: Int, v2: Int) => v1 + v2, 
        Seconds(60), 
        Seconds(10))  
    
    categoryProductCountsDStream.foreachRDD(categoryProductCountsRDD => {
      val categoryProductCountRowRDD = categoryProductCountsRDD.map(tuple => {
        val category = tuple._1.split("_")(0)
        val product = tuple._1.split("_")(1)  
        val count = tuple._2
        Row(category, product, count)  
      })
      
      val structType = StructType(Array(
          StructField("category", StringType, true),
          StructField("product", StringType, true),
          StructField("click_count", IntegerType, true)))
          
      val hiveContext = new HiveContext(categoryProductCountsRDD.context)
      
      val categoryProductCountDF = hiveContext.createDataFrame(categoryProductCountRowRDD, structType)  
      
      categoryProductCountDF.registerTempTable("product_click_log")  
      
      val top3ProductDF = hiveContext.sql(
            "SELECT category,product,click_count "
            + "FROM ("
              + "SELECT "
                + "category,"
                + "product,"
                + "click_count,"
                + "row_number() OVER (PARTITION BY category ORDER BY click_count DESC) rank "
              + "FROM product_click_log"  
            + ") tmp "
            + "WHERE rank<=3")
            
      top3ProductDF.show()
    })
    
    ssc.start()
    ssc.awaitTermination()
  }
  
}