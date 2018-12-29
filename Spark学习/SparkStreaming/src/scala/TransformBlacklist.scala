package cn.spark.study.streaming


import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds

object TransformBlacklist {
    def main(args: Array[String]): Unit = {
      val conf = new SparkConf()
          .setMaster("local[2]")  
          .setAppName("TransformBlacklist")
      val ssc = new StreamingContext(conf, Seconds(5))
      
      val blackList = Array(("tom", true))
      val blackListRDD = ssc.sparkContext.parallelize(blackList, 5)
      
      val adsClickLogDStream = ssc.socketTextStream("spark1", 9999)
      val userAdsClickLogDStream = adsClickLogDStream
      .map { adsClickLog => (adsClickLog.split(" ")(1), adsClickLog) }
      
      val validAdsClickLogDStream = userAdsClickLogDStream.transform(userAdsClickLogRDD => {
        val joinedRDD = userAdsClickLogRDD.leftOuterJoin(blackListRDD)
        val filteredRDD = joinedRDD.filter( tuple => {
          if(tuple._2._2.getOrElse(false)) {  
            false
          } else {
            true
        }
        })
        val validAdsClickLogRDD = filteredRDD.map(tuple => tuple._2._1) 
        validAdsClickLogRDD
      })
      
      validAdsClickLogDStream.print()
    
      ssc.start()
      ssc.awaitTermination()

    }
}