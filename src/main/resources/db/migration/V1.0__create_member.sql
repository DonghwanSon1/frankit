-- 유저 테이블 (users)
CREATE TABLE IF NOT EXISTS member (
    `sn` BIGINT AUTO_INCREMENT PRIMARY KEY,        -- 유저 SN
    `email` VARCHAR(30) NOT NULL,                  -- 사용자 ID (고유)
    `password` VARCHAR(100) NOT NULL,              -- 비밀번호
    `name` VARCHAR(10) NOT NULL,                   -- 이름
    `store_name` VARCHAR(100) NOT NULL,            -- 가게 이름
    `role` VARCHAR(30) NOT NULL,                   -- 역할
    UNIQUE KEY unique_id (`email`)                 -- 유저 테이블 유니크 키 관계
);