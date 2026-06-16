-- 创建数据库
CREATE DATABASE IF NOT EXISTS tiktok DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE tiktok;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(32) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 视频表
CREATE TABLE IF NOT EXISTS `video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `video_url` VARCHAR(255) NOT NULL COMMENT '视频存储路径',
    `title` VARCHAR(128) NOT NULL COMMENT '视频标题',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_like_count` (`like_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- 点赞记录表
CREATE TABLE IF NOT EXISTS `video_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 插入测试用户
INSERT INTO `user` (`id`, `username`, `password`) VALUES
(1, 'alice', '123456'),
(2, 'bob', '123456'),
(3, 'charlie', '123456');

-- 插入测试视频（引用 service/video/ 目录下的文件）
INSERT INTO `video` (`id`, `user_id`, `video_url`, `title`, `like_count`) VALUES
(1, 1, 'service/video/sample1.mp4', '测试视频1 - 风景航拍', 5),
(2, 2, 'service/video/sample2.mp4', '测试视频2 - 城市漫步', 3),
(3, 3, 'service/video/sample3.mp4', '测试视频3 - 自然风光', 8);

-- bob 给视频1和视频3点赞
INSERT INTO `video_like` (`user_id`, `video_id`) VALUES
(2, 1),
(2, 3);
