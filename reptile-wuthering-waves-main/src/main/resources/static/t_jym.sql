/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50722
 Source Host           : localhost:3306
 Source Schema         : reptile

 Target Server Type    : MySQL
 Target Server Version : 50722
 File Encoding         : 65001

 Date: 24/02/2025 16:52:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_jym
-- ----------------------------
DROP TABLE IF EXISTS `t_jym`;
CREATE TABLE `t_jym`  (
  `product_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `rc_token` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `game_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '游戏ID',
  `price` bigint(20) NULL DEFAULT NULL COMMENT '价格',
  `product_unique_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品唯一号',
  `product_name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '商品名称',
  `product_type` int(11) NULL DEFAULT NULL COMMENT '商品类型',
  `shelve_up_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上架时间',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '商品创建时间',
  `main_image_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主图',
  `arms_count` int(11) NULL DEFAULT NULL COMMENT '武器数量',
  `role_count` int(11) NULL DEFAULT NULL COMMENT '角色数量',
  `lottery_count` int(11) NULL DEFAULT NULL COMMENT '抽奖次数',
  `pay_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '下单链接',
  `details_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详情链接',
  `seller_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '卖家id',
  PRIMARY KEY (`product_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
