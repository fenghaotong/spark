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
/*Table structure for table `city_info` */

DROP TABLE IF EXISTS `city_info`;

CREATE TABLE `city_info` (
  `city_id` int(11) DEFAULT NULL,
  `city_name` varchar(255) DEFAULT NULL,
  `area` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `city_info` */

insert  into `city_info`(`city_id`,`city_name`,`area`) values (0,'Beijing','China North'),(1,'Shanghai','China East'),(2,'Nanjing','China East'),(3,'Guangzhou','China South'),(4,'Sanya','China South'),(5,'Wuhan','China Middle'),(6,'Changsha','China Middle'),(7,'Xian','West North'),(8,'Chengdu','West South'),(9,'Haerbin','East North');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
