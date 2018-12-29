# 通用的load和save操作

## 通用的load和save操作

- 对于Spark SQL的DataFrame来说，无论是从什么数据源创建出来的DataFrame，都有一些共同的load和save操作。load操作主要用于加载数据，创建出DataFrame；save操作，主要用于将DataFrame中的数据保存到文件中。

Java版本

```java
DataFrame df = sqlContext.read().load("users.parquet");

df.select("name","favorite_color").write().save("namesAndFavColors.parquet");
```

[Java版本](src/java/GenericLoadSave.java)

Scala版本

```scala
val df = sqlContext.read.load("users.parquet")

df.select("name","favorite_color").write.save("namesAndFavColors.parquet")
```

[Scala版本](src/scala/GenericLoadSave.scala)

## 手动指定数据源类型

- 也可以手动指定用来操作的数据源类型。数据源通常需要使用其全限定名来指定，比如parquet是`org.apache.spark.sql.parquet`。但是Spark SQL内置了一些数据源类型，比如json，parquet，jdbc等等。实际上，通过这个功能，就可以在不同类型的数据源之间进行转换了。比如将json文件中的数据保存到parquet文件中。默认情况下，如果不指定数据源类型，那么就是parquet。

Java版本

```java
DataFrame df = sqlContext.read().format("json").load("people.json");
df.select("name","age").write().format("parquet").save("namesAndAges.parquet");

```

[Java版本](src/java/ManuallySpecifyOptions.java)

Scala版本

```scala
val df = sqlContext.read.format("json").load("people.json")
df.select("name","age").write.format("parquet").save("namesAndAges.parquet")

```

[Scala版本](src/scala/ManuallySpecifyOptions.scala)

## Save Mode

- Spark SQL对于save操作，提供了不同的save
  mode。主要用来处理，当目标位置，已经有数据时，应该如何处理。而且save操作并不会执行锁操作，并且不是原子的，因此是有一定风险出现脏数据的。

| Save Mode                     | 意义                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| SaveMode.ErrorIfExists (默认) | 如果目标位置已经存在数据，那么抛出一个异常                   |
| SaveMode.Append               | 如果目标位置已经存在数据，那么将数据追加进去                 |
| SaveMode.Overwrite            | 如果目标位置已经存在数据，那么就将已经存在的数据删除，用新数据进行覆盖 |
| SaveMode.Ignore               | 如果目标位置已经存在数据，那么就忽略，不做任何操作。         |

[Java版本](src/java/SaveModeTest.java)