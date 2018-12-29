# Spark 1.x的Volcano Iterator Model深度剖析

深入剖析Spark 2.x的第二代tungsten引擎原理之前，先看一下当前的Spark的工作原理。我们可以通过一个SQL来举例，这个SQL扫描了单个表，然后对属性等于指定值的记录进行汇总计数。SQL语句如下：

```sql
select count(*) from store_sales where ss_item_sk=1000
```



要执行这个查询，Spark 1.x会使用一种最流行、最经典的查询求值策略，该策略主要基于Volcano Iterator Model。在这种模型中，一个查询会包含多个operator，每个operator都会实现一个接口，提供一个next()方法，该方法返回operator tree中的下一个operator。

让每一个operator都实现一个iterator接口，可以让查询引擎优雅的组装任意operator在一起。而不需要查询引擎去考虑每个operator具体的一些处理逻辑，比如数据类型等。

Vocano Iterator Model也因此成为了数据库SQL执行引擎领域内内的20年中最流行的一种标准。而且Spark SQL最初的SQL执行引擎也是基于这个思想来实现的。

手写的代码的性能比Volcano Iterator Model高了一整个数量级，而这其中的原因包含以下几点：

1. **避免了virtual function dispatch**：在Volcano Iterator Model中，至少需要调用一次next()函数来获取下一个operator。这些函数调用在操作系统层面，会被编译为virtual function dispatch。而手写代码中，没有任何的函数调用逻辑。虽然说，现代的编译器已经对虚函数调用进行了大量的优化，但是该操作还是会执行多个CPU指令，并且执行速度较慢，尤其是当需要成百上千次地执行虚函数调用时。
2. **通过CPU Register存取中间数据，而不是内存缓冲**：在Volcano Iterator Model中，每次一个operator将数据交给下一个operator，都需要将数据写入内存缓冲中。然而在手写代码中，JVM JIT编译器会将这些数据写入CPU Register。CPU从内存缓冲种读写数据的性能比直接从CPU Register中读写数据，要低了一个数量级。
3. **Loop Unrolling和SIMD**：现代的编译器和CPU在编译和执行简单的for循环时，性能非常地高。编译器通常可以自动对for循环进行unrolling，并且还会生成SIMD指令以在每次CPU指令执行时处理多条数据。CPU也包含一些特性，比如pipelining，prefetching，指令reordering，可以让for循环的执行性能更高。然而这些优化特性都无法在复杂的函数调用场景中施展，比如Volcano Iterator Model。