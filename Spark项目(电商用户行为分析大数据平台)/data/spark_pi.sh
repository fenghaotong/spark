/usr/local/spark/bin/spark-submit \
--class org.apache.spark.examples.JavaSparkPi \
--master yarn-client \
--num-executors 1 \
--driver-memory 100m \
--executor-memory 100m \
--executor-cores 1 \
/usr/local/spark/lib/spark-examples-1.5.1-hadoop2.4.0.jar \

