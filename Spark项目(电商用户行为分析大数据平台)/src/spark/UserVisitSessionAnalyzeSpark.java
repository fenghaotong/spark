package com.htfeng.sparkproject.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;

import com.htfeng.sparkproject.conf.ConfigurationManager;
import com.htfeng.sparkproject.constant.Constant;
import com.htfeng.sparkproject.test.MockData;

/**
 * 用户访问session分析Spark作业
 * @author htfeng
 *
 */
public class UserVisitSessionAnalyzeSpark {
    public static void main(String[] args) {
        // 构建Spark上下文
        SparkConf conf = new SparkConf()
                .setAppName(Constant.SPARK_APP_NAME_SESSION)
                .setMaster("local");
        
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = getSQLContext(sc.sc());
        
        // 生成模拟测试数据
        mockData(sc, sqlContext);
        
        // 关闭上下文
        sc.close();
    }
    
    
    /**
     * 获取SQLContext
     * 如果是在本地测试环境的话，那么就生成SQLContext对象
     * 如果是在生产环境运行的话，那么就生成HiveContext对象
     * @param sc SparkContext
     * @return SQLContext
     */
    private static SQLContext getSQLContext(SparkContext sc) {
        boolean local = ConfigurationManager.getBoolean(Constant.SPARK_LOCAL);
        if (local) {
            return new SQLContext(sc);
        } else {
            return new HiveContext(sc);
        }
    }
    
    /**
     * 生成模拟数据（只有本地模式，才会去生成模拟数据）
     * @param sc
     * @param sqlContext
     */
    private static void mockData(JavaSparkContext sc, SQLContext sqlContext) {
        boolean local = ConfigurationManager.getBoolean(Constant.SPARK_LOCAL);
        if (local) {
            MockData.mock(sc, sqlContext);
        }
    }
}
