/*
Navicat MySQL Data Transfer

Source Server         : local_mysql
Source Server Version : 50617
Source Host           : localhost:3306
Source Database       : morningtech

Target Server Type    : MYSQL
Target Server Version : 50617
File Encoding         : 65001

Date: 2018-06-13 16:44:56
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `sys_eth_contract`
-- ----------------------------
DROP TABLE IF EXISTS `sys_eth_contract`;
CREATE TABLE `sys_eth_contract` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coinname` varchar(20) NOT NULL,
  `contractBinary` text COMMENT '合约创建代码',
  `contractAddress` varchar(100) DEFAULT NULL COMMENT '合约地址',
  `gasPrice` decimal(20,8) DEFAULT '8.00000000' COMMENT '燃料价格Gwei',
  `gasLimit` decimal(20,0) DEFAULT '80000' COMMENT '燃料数量',
  `maxMentionLimit` decimal(20,8) DEFAULT NULL,
  `status` int(1) DEFAULT '0' COMMENT '0关闭部署，1开启部署',
  PRIMARY KEY (`id`),
  UNIQUE KEY `INDEX_UNIQUE_COINNAME` (`coinname`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_eth_mention`
-- ----------------------------
DROP TABLE IF EXISTS `sys_eth_mention`;
CREATE TABLE `sys_eth_mention` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `txid` varchar(200) DEFAULT NULL COMMENT '提币记录唯一ID',
  `userid` int(11) unsigned NOT NULL,
  `username` varchar(200) NOT NULL COMMENT '用户账号-冗余',
  `coinname` varchar(200) NOT NULL COMMENT '币种编码',
  `mentionaddress` varchar(200) DEFAULT NULL COMMENT '提币地址',
  `num` decimal(20,8) unsigned NOT NULL COMMENT '提币数量',
  `addtime` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '提交时间',
  `endtime` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '转账完成时间',
  `status` int(4) NOT NULL DEFAULT '0' COMMENT '转账状态，0默认等待处理，1成功，-1失败，-2取消',
  `hash` varchar(200) DEFAULT NULL COMMENT '交易hash',
  `isdeleted` int(1) DEFAULT '0' COMMENT '0默认，1删除',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注',
  `islock` int(1) DEFAULT '0' COMMENT '是否锁定，0未锁定，1锁定',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_myzc_hashid` (`hash`) USING BTREE,
  KEY `userid` (`userid`) USING BTREE,
  KEY `status` (`status`) USING BTREE,
  KEY `coinname` (`coinname`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for `sys_eth_transfer`
-- ----------------------------
DROP TABLE IF EXISTS `sys_eth_transfer`;
CREATE TABLE `sys_eth_transfer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coinName` varchar(20) DEFAULT NULL COMMENT '币种编码，合约名称',
  `from` varchar(100) NOT NULL COMMENT '转出地址',
  `to` varchar(100) NOT NULL COMMENT '转入地址',
  `contractAddress` varchar(100) DEFAULT NULL COMMENT '合约地址',
  `value` decimal(50,0) NOT NULL COMMENT '交易金额',
  `eth` decimal(20,8) DEFAULT NULL,
  `blockNumber` varchar(50) DEFAULT NULL,
  `transactionHash` varchar(200) DEFAULT NULL COMMENT '交易hash',
  `gasUsed` decimal(50,0) DEFAULT NULL COMMENT '交易手续费单位Wei',
  `sysTime` datetime DEFAULT NULL COMMENT '系统生成时间',
  `pendingTime` datetime DEFAULT NULL COMMENT '创建等待事物的时间',
  `transTime` datetime DEFAULT NULL COMMENT '交易完成时间',
  `transMainTime` datetime DEFAULT NULL COMMENT '转移到主账户时间',
  `status` int(1) DEFAULT NULL COMMENT '0:待处理，1.正在转账，2，转账成功，-1，转账失败',
  `tranSign` varchar(200) DEFAULT NULL COMMENT '交易数据加密',
  `remark` text,
  `outTransactionHash` varchar(200) DEFAULT NULL COMMENT '转出hash',
  `tranStatus` int(1) DEFAULT '0' COMMENT '0:默认，1正在转入eth矿工费，2正在转出，3转出成功，-1失败或取消',
  `isDeleted` int(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_eth_transfer_transhash` (`transactionHash`) USING BTREE,
  UNIQUE KEY `uq_eth_transfer_tranSign` (`tranSign`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- ----------------------------
-- Records of sys_eth_transfer
-- ----------------------------

-- ----------------------------
-- Table structure for `sys_wallet_account`
-- ----------------------------
DROP TABLE IF EXISTS `sys_wallet_account`;
CREATE TABLE `sys_wallet_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `accountId` varchar(50) NOT NULL COMMENT '钱包地址',
  `password` varchar(100) NOT NULL COMMENT '钱包密码',
  `keystorePath` varchar(100) NOT NULL COMMENT 'keystore文件路径',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wallet_accountId` (`accountId`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=1510 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `sys_wallet_option`
-- ----------------------------
DROP TABLE IF EXISTS `sys_wallet_option`;
CREATE TABLE `sys_wallet_option` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coinname` varchar(10) NOT NULL COMMENT '币种编码，合约名称',
  `minethlimit` decimal(20,8) DEFAULT NULL COMMENT '最小提取数量，单位ETHER',
  `isopen` int(1) DEFAULT '0' COMMENT '默认0关闭，1开启提币',
  `opentime` datetime DEFAULT NULL COMMENT '提币开启时间，用于自动开启',
  `isdeleted` int(11) DEFAULT '0' COMMENT '是否删除，0不删除，1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


drop table if exists `sys_wallet_account_balance`;
create table `sys_wallet_account_balance`(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(50) NOT NULL COMMENT '钱包地址',
  `coinname` varchar(10) NOT NULL COMMENT '币种编码，合约名称',
  `balance` decimal (20, 8) comment '余额，单位 ether',
  `lasttime` datetime comment '更新时间',
  primary  key(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 ;
-- ----------------------------
-- Records of sys_wallet_option
-- ----------------------------
--
-- drop table if exists `sys_eth_configuration`;
-- create table `sys_eth_configuration`(
--  `id` int(11) not null AUTO_INCREMENT,
--  `key` varchar(50) not null comment 'key',
--  `value` text not null comment'value',
--  `remark` varchar(100) comment'备注',
--  primary key (`id`)
--
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
