/usr/local/spark/bin/spark-submit \
--class com.htfeng.sparkproject.spark.session.UserVisitSessionAnalyzeSpark \
--num-executors 1 \
--driver-memory 100m \
--executor-memory 100m \
--executor-cores 1 \
--files /usr/local/hive/conf/hive-site.xml \
--driver-class-path /usr/local/hive/lib/mysql-connector-java-5.1.17.jar \
/usr/local/spark-study/spark-project-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
${1}
