CREATE TABLE `t_px` (
                        `product_id` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
                        `rc_token` varchar(256) COLLATE utf8mb4_general_ci NOT NULL,
                        `status` int NOT NULL COMMENT '状态',
                        `game_id` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '游戏ID',
                        `price` bigint DEFAULT NULL COMMENT '价格',
                        `product_unique_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品唯一号',
                        `product_name` text COLLATE utf8mb4_general_ci COMMENT '商品名称',
                        `product_type` int DEFAULT NULL COMMENT '商品类型',
                        `shelve_up_time` datetime DEFAULT NULL COMMENT '上架时间',
                        `create_time` datetime DEFAULT NULL COMMENT '商品创建时间',
                        `main_image_url` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主图',
                        `arms_count` int DEFAULT NULL COMMENT '武器数量',
                        `role_count` int DEFAULT NULL COMMENT '角色数量',
                        `lottery_count` int DEFAULT NULL COMMENT '抽奖次数',
                        PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;