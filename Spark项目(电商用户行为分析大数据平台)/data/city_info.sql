/*
SQLyog Ultimate v11.25 (64 bit)
MySQL - 5.5.20 : Database - spark_project
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`spark_project` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `spark_project`;

/*Table structure for table `city_info` */

DROP TABLE IF EXISTS `city_info`;

CREATE TABLE `city_info` (
  `city_id` int(11) DEFAULT NULL,
  `city_name` varchar(255) DEFAULT NULL,
  `area` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `city_info` */

insert  into `city_info`(`city_id`,`city_name`,`area`) values (0,'北京','华北'),(1,'上海','华东'),(2,'南京','华东'),(3,'广州','华南'),(4,'三亚','华南'),(5,'武汉','华中'),(6,'长沙','华中'),(7,'西安','西北'),(8,'成都','西南'),(9,'哈尔滨','东北');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
