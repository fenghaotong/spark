/usr/local/spark/bin/spark-submit \
--class cn.spark.study.core.WordCount \
--num-executors 1 \
--driver-memory 100m \
--executor-memory 100m \
--executor-cores 1 \
/usr/local/test/spark-study-java-0.0.1-SNAPSHOT-jar-with-dependencies.jar \