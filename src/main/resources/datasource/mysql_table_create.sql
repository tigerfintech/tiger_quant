CREATE DATABASE tiger_quant;
USE tiger_quant;

CREATE TABLE `bar` (
	`id` INT(10) NOT NULL AUTO_INCREMENT,
    `symbol` varchar(20) NOT NULL DEFAULT 0,
    `duration` bigint NOT NULL DEFAULT 0,
    `open`double(15,4) NOT NULL DEFAULT 0,
    `high`double(15,4) NOT NULL DEFAULT 0,
    `low`double(15,4) NOT NULL DEFAULT 0,
    `close`double(15,4) NOT NULL DEFAULT 0,
    `volume` int NOT NULL DEFAULT 0,
    `amount` double(15,4) NOT NULL DEFAULT 0,
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	  PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;