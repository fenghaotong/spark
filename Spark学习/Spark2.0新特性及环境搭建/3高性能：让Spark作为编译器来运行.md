# 高性能：让Spark作为编译器来运行

Spark 2.0的一个重大的特点就是搭载了最新的第二代tungsten引擎。第二代tungsten引擎吸取了现代编译器以及并行数据库的一些重要的思想，并且应用在了spark的运行机制中。其中一个核心的思想，就是在运行时动态地生成代码，在这些自动动态生成的代码中，可以将所有的操作都打包到一个函数中，这样就可以避免多次virtual function call，而且还可以通过cpu  register来读写中间数据，而不是通过cpu cache来读写数据。上述技术整体被称作“whole-stage
code generation”，中文也可以叫“全流程代码生成”。



spark 2.0中，除了whole-stage code generation技术以外，还使用了其他一些新技术来提升性能。比如说对Spark SQL的catalyst查询优化器做了一些性能优化，来提升对一些常见查询的优化效率，比如null值处理等。再比如说，通过vectarization技术将parquet文件扫描的吞吐量提升了3倍以上。