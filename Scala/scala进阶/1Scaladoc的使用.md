# Scaladoc的使用

- Scaladoc是什么：scala api文档，包含了scala所有的api以及使用说明，`class、object、trait、function、method、implicit`等
- 为什么要查阅Scaladoc：如果只是写一些普通的Scala程序，课程中讲解（Scala编程详解）的内容基本够用了；但是如果（在现在，或者未来，实际的工作环境中）要编写复杂的scala程序，那么还是需要参考Scaladoc的。（纯粹用scala开发spark应用程序，应该不会特别复杂；用scala构建类似于spark的公司内的分布式的大型系统）
- 通过url：http://www.scala-lang.org/api/current/#package，可以在线浏览Scaladoc
- 以下是一些Scaladoc使用的tips（小贴士，小备注）：
  1. 直接在左上角的搜索框中，搜索你需要的寻找的包、类即可
  2. O和C，分别代表了某个类的伴生对象以及伴生类的概念
  3. 标记为implicit的方法，代表的是隐式转换
  4. 举例：搜索StringOps，可以看到String的增强类，StringOps的所有方法说明