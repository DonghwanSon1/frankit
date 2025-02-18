-- 선택 옵션 테이블 (SelectOption)
CREATE TABLE IF NOT EXISTS select_option (
    `sn` BIGINT AUTO_INCREMENT PRIMARY KEY,                     -- 선택 옵션 SN
    `name` VARCHAR(100) NOT NULL                                -- 선택 옵션 이름
);