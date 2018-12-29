/root/spark/bin/spark-submit \
--class org.leo.spark.SparkSQLTest \
--num-executors 3 \
--driver-memory 100m \
--executor-memory 100m \
--executor-cores 3 \
--files /root/hive/conf/hive-site.xml \
--driver-class-path /root/hive/lib/mysql-connector-java-5.1.17.jar \
/root/spark-test-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
