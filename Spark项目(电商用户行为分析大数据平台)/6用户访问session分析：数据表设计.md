# 用户访问session分析：数据表设计

在进行完了数据调研、需求分析、技术实现方案，进行数据设计。数据设计，往往包含两个环节，第一个呢，就是说，我们的上游数据，就是数据调研环节看到的项目基于的基础数据，是否要针对其开发一些Hive ETL，对数据进行进一步的处理和转换，从而让我们能够更加方便的和快速的去计算和执行spark作业；第二个，就是要设计spark作业要保存结果数据的业务表的结构，从而让J2EE平台可以使用业务表中的数据，来为使用者展示任务执行结果。

**第一表：session_aggr_stat表**

存储第一个功能，session聚合统计的结果

```mysql
CREATE TABLE `session_aggr_stat` (
  `task_id` int(11) NOT NULL,
  `session_count` int(11) DEFAULT NULL,
  `1s_3s` double DEFAULT NULL,
  `4s_6s` double DEFAULT NULL,
  `7s_9s` double DEFAULT NULL,
  `10s_30s` double DEFAULT NULL,
  `30s_60s` double DEFAULT NULL,
  `1m_3m` double DEFAULT NULL,
  `3m_10m` double DEFAULT NULL,
  `10m_30m` double DEFAULT NULL,
  `30m` double DEFAULT NULL,
  `1_3` double DEFAULT NULL,
  `4_6` double DEFAULT NULL,
  `7_9` double DEFAULT NULL,
  `10_30` double DEFAULT NULL,
  `30_60` double DEFAULT NULL,
  `60` double DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```



**第二个表：session_random_extract表**

存储我们的按时间比例随机抽取功能抽取出来的1000个session

```mysql
CREATE TABLE `session_random_extract` (
  `task_id` int(11) NOT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `start_time` varchar(50) DEFAULT NULL,
  `end_time` varchar(50) DEFAULT NULL,
  `search_keywords` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

```

**第三个表：top10_category表**

存储按点击、下单和支付排序出来的top10品类数据

```mysql
CREATE TABLE `top10_category` (
  `task_id` int(11) NOT NULL,
  `category_id` int(11) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL,
  `order_count` int(11) DEFAULT NULL,
  `pay_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


```

**第四个表：top10_category_session表**

存储top10每个品类的点击top10的session

```mysql
CREATE TABLE `top10_category_session` (
  `task_id` int(11) NOT NULL,
  `category_id` int(11) DEFAULT NULL,
  `session_id` varchar(200) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

**第五张表：session_detail**

用来存储随机抽取出来的session的明细数据、top10品类的session的明细数据

```mysql
CREATE TABLE `session_detail` (
  `task_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `page_id` int(11) DEFAULT NULL,
  `action_time` varchar(255) DEFAULT NULL,
  `search_keyword` varchar(255) DEFAULT NULL,
  `click_category_id` int(11) DEFAULT NULL,
  `click_product_id` int(11) DEFAULT NULL,
  `order_category_ids` varchar(255) DEFAULT NULL,
  `order_product_ids` varchar(255) DEFAULT NULL,
  `pay_category_ids` varchar(255) DEFAULT NULL,
  `pay_product_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

```

**第六张表：task表**

用来存储J2EE平台插入其中的任务的信息

```mysql
CREATE TABLE `task` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(255) DEFAULT NULL,
  `create_time` varchar(255) DEFAULT NULL,
  `start_time` varchar(255) DEFAULT NULL,
  `finish_time` varchar(255) DEFAULT NULL,
  `task_type` varchar(255) DEFAULT NULL,
  `task_status` varchar(255) DEFAULT NULL,
  `task_param` text,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8
```

在数据设计以后，就正式进入一个漫长的环节，就是编码实现阶段，coding阶段。在编码实现阶段，每开发完一个功能，其实都会走后续的两个环节，就是本地测试和生产环境测试。

大家需要在windows上面，自己安装MySQL数据库。然后本地测试的时候，将数据插入本地的MySQL中。

接下来，就是在完成了数据调研、需求分析、技术方案设计、数据设计以后，正式进入编码实现和功能测试阶段。最后才是性能调优阶段。