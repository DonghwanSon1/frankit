-- 상품 테이블 (Product)
CREATE TABLE IF NOT EXISTS product (
    `sn` BIGINT AUTO_INCREMENT PRIMARY KEY,                     -- 상품 SN
    `name` VARCHAR(100) NOT NULL,                               -- 상품 이름
    `description` TEXT NOT NULL,                                -- 상품 설명
    `price` INT UNSIGNED NOT NULL,                              -- 가격
    `shipping_fee` MEDIUMINT UNSIGNED NOT NULL,                 -- 배송비
    `status` TINYINT NOT NULL DEFAULT 1,                        -- 상태
    `is_delete` BOOLEAN NOT NULL DEFAULT FALSE,                 -- 삭제 여부
    `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 상품 등록일
    `delete_date` TIMESTAMP,                                    -- 상품 삭제일
    INDEX idx_name_is_delete (`name`, `is_delete`),              -- 상품 이름, 삭제 여부 인덱스
    INDEX idx_is_delete (`is_delete`)                           -- 상품 삭제 여부 인덱스
);