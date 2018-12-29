# SparkSQLCLI命令行使用

- Spark SQL CLI是一个很方便的工具，可以用来在本地模式下运行Hive的元数据服务，并且通过命令行执行针对Hive的SQL查询。但是

- 要注意的是，Spark SQL CLI是不能与Thrift JDBC server进行通信的。

- 如果要启动Spark SQL CLI，只要执行Spark的bin目录下的spark-sql命令即可

  ```sh
  ./bin/spark-sql --jars /usr/local/hive/lib/mysql-connector-java-5.1.17.jar
  ```

- 同样要注意的是，必须将我们的hive-site.xml文件放在Spark的conf目录下。

- 也可以通过执行./bin/spark-sql --help命令，来获取该命令的所有帮助选项。

