-- 创建 visitor 表
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

-- 插入初始 visitor 账户
-- 用户名：visitor
-- 密码：123456 (BCrypt 加密后的密码)
INSERT INTO `visitor` (username, password, status) 
VALUES ('visitor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lqkkO9QS3TzCjH3rS', 0);

-- 注意：上面的密码是使用 BCrypt 加密的"123456"
-- 如果登录失败，请使用以下 SQL 重新生成密码（需要 Java 后端运行）
-- 或者在 Java 代码中使用 BCryptPasswordEncoder 生成新密码
