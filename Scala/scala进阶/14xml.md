# xml

### Scala中xml基础操作

**scala中定义xml**

- scala对xml有很好的支持，可以直接在scala代码中定义一个xml文档元素

  ```scala
  val books = <books><book>my first scala book</book></books>
  ```

  > 此时doc的类型是scala.xml.Elem，也就是一个xml元素

- scala还可以直接定义多个同级别的xml元素

  ```scala
  val books = <book>my first scala book</book><book>my first spark book</book>
  ```

  > 此时doc的类型是scala.xml.NodeBuffer，也就是一个xml节点序列

**XML节点类型**

- Node类是所有XML节点类型的父类型，两个重要的子类型是Text和Elem。

- Elem表示一个XML元素，也就是一个XML节点。scala.xml.Elem类型的label属性，返回的是标签名，child属性，返回的是子元素。

- `scala.xml.NodeSeq`类型，是一个元素序列，可以用for循环，直接遍历它。

- 可以通过`scala.xml.NodeBuffe`r类型，来手动创建一个节点序列

  ```scala
  val booksBuffer = new scala.xml.NodeBuffer
  booksBuffer += <book>book1</book>
  booksBuffer += <book>book2</book>
  val books: scala.xml.NodeSeq = booksBuffer
  ```

**xml元素的属性**

- `scala.xml.Elem.attributes`属性，可以返回这儿xml元素的属性，是`Seq[scala.xml.Node]`类型的，继续调用text属性，可以拿到属性的值

  ```scala
  val book = <book id=“1” price=“10.0”>book1</book>
  val bookId = book.attributes(“id”).text
  ```

- 还可以遍历属性

  ```scala
  for(attr <- book.attributes) println(attr)
  ```

  > 还可以调用`book.attributes.asAttrMap`，获取一个属性Map

### 在xml中嵌入scala代码

**在xml中嵌入scala代码**

```scala
val books = Array("book1", "book2")

<books><book>{ books(0) }</book><book>{ books(1) }</book></books>
<books>{ for (book <- books) yield <book>{book}</book> }</books>
```

**还可以在xml属性中嵌入scala代码**

```scala
<book id={ books(0) }>{ books(0) }</book>
```



### 修改xml元素

**默认情况下，scala中的xml表达式是不可改变的；如果要修改xml元素的话，必须拷贝一份再修改**

```scala
import scala.xml._
val books = <books><book>book1</book></books>
```

**添加一个子元素**

```scala
val booksCopy = books.copy(child = books.child ++ <book>book2</book>)

val book = <book id="1">book1</book>
```

**修改一个属性**

```scala
val bookCopy = book % Attribute(null, "id", "2", Null)
```

**添加一个属性**

```scala
val bookCopy = book % Attribute(null, "id", "2", Attribute(null, "price", "10.0", Null))
```

### xml 加载和写入外部xml文件

**使用scala的XML类加载**

```scala
import scala.xml._
import java.io._
val books = XML.loadFile("books.xml")
```

**使用Java的FileInputStream类加载**

```scala
val books = XML.load(new FileInputStream("books.xml"))
```

**使用Java的InputStreamReader类指定加载编码**

```scala
val books = XML.load(new InputStreamReader(new FileInputStream("books.xml"), "UTF-8"))
```

**将内存中的xml对象，写入外部xml文档**

```scala
XML.save("books2.xml", books)
```



