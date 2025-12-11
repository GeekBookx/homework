CREATE DATABASE IF NOT EXISTS lab_reservation DEFAULT CHARSET utf8mb4;
USE lab_reservation;

-- 1. 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN') NOT NULL,
    full_name VARCHAR(50),
    -- 状态：0-待审核(仅Manager需要), 1-正常, 2-禁用
    status TINYINT DEFAULT 1, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 实验室表
CREATE TABLE labs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL, -- 容量
    equipment_list TEXT,   -- 设备清单
    is_active BOOLEAN DEFAULT TRUE, -- 是否开放（用于维护期设置）
    description TEXT
);

-- 3. 预约记录表
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lab_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    -- 状态：PENDING-待审核, APPROVED-已通过, REJECTED-已驳回, COMPLETED-已完成
    status VARCHAR(20) DEFAULT 'PENDING', 
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lab_id) REFERENCES labs(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 4. 公告表 (用于登录页展示)
CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    is_current BOOLEAN DEFAULT FALSE, -- 标记当前显示的公告
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化管理员账号 (密码建议加密，此处演示用明文)
INSERT INTO users (username, password, role, full_name, status) 
VALUES ('admin', 'admin123', 'ADMIN', '系统管理员', 1);

-- 初始化一条公告
INSERT INTO announcements (content, is_current) VALUES ('欢迎使用高校实验室预约管理系统，请规范使用设备。', 1);