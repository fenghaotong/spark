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
/*Table structure for table `ad_blacklist` */

DROP TABLE IF EXISTS `ad_blacklist`;

CREATE TABLE `ad_blacklist` (
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `ad_blacklist` */

/*Table structure for table `ad_click_trend` */

DROP TABLE IF EXISTS `ad_click_trend`;

CREATE TABLE `ad_click_trend` (
  `date` varchar(30) DEFAULT NULL,
  `hour` varchar(30) DEFAULT NULL,
  `minute` varchar(30) DEFAULT NULL,
  `ad_id` int(11) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `ad_click_trend` */

/*Table structure for table `ad_province_top3` */

DROP TABLE IF EXISTS `ad_province_top3`;

CREATE TABLE `ad_province_top3` (
  `date` varchar(30) DEFAULT NULL,
  `province` varchar(100) DEFAULT NULL,
  `ad_id` int(11) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `ad_province_top3` */

/*Table structure for table `ad_stat` */

DROP TABLE IF EXISTS `ad_stat`;

CREATE TABLE `ad_stat` (
  `date` varchar(30) DEFAULT NULL,
  `province` varchar(100) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `ad_id` int(11) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `ad_stat` */

/*Table structure for table `ad_user_click_count` */

DROP TABLE IF EXISTS `ad_user_click_count`;

CREATE TABLE `ad_user_click_count` (
  `date` varchar(30) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `ad_id` int(11) DEFAULT NULL,
  `click_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `ad_user_click_count` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
