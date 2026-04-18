-- 快速初始化 visitor 账户的 SQL 脚本
-- 此脚本使用 MD5 加密的密码 "123456"
-- MD5("123456") = e10adc3949ba59abbe56e057f20f883e

-- 如果表不存在则创建
CREATE TABLE IF NOT EXISTS `visitor`(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(50),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 0 COMMENT '0:启用，1:禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 删除已存在的 visitor 账户（可选）
-- DELETE FROM `visitor` WHERE username = 'visitor';

-- 插入 visitor 账户
-- 用户名：visitor
-- 密码：123456 (MD5 加密)
INSERT INTO `visitor` (username, password, status, create_time, update_time) 
VALUES ('visitor', 'e10adc3949ba59abbe56e057f20f883e', 0, NOW(), NOW());

-- 验证插入
SELECT * FROM `visitor` WHERE username = 'visitor';
