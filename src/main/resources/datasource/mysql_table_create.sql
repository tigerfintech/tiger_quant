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

CREATE TABLE `contract` (
	`id` INT(10) NOT NULL AUTO_INCREMENT,
	`identifier` varchar(40) NOT NULL,
	`name` varchar(50) DEFAULT NULL,
    `symbol` varchar(20) NOT NULL DEFAULT '',
    `sec_type` varchar(20) DEFAULT NULL,
    `currency` varchar(10) DEFAULT NULL,
    `exchange` varchar(10) DEFAULT NULL,
    `market` varchar(10) DEFAULT NULL,
    `expiry` varchar(10) DEFAULT NULL,
    `contract_month` varchar(10) DEFAULT NULL,
    `strike` double DEFAULT NULL,
    `multiplier` double DEFAULT NULL,
    `right` varchar(10) DEFAULT NULL,
    `min_tick` double DEFAULT NULL,
    `lot_size` int DEFAULT 0,
    `create_time` timestamp NOT NULL DEFAULT '2018-01-01 10:00:00',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`),
    KEY `idx_identifier` (`identifier`),
	KEY `idx_symbol` (`symbol`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
