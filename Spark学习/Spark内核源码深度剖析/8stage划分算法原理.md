# stage划分算法原理



![](img\stage划分算法原理剖析.png)

**stage划分算法总结**

1. 从finalStage倒推
2. 通过宽依赖，来进行新的stage划分
3. 通过递归，优先提交父stage