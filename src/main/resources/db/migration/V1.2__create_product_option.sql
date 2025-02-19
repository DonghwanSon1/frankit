-- 상품 옵션 테이블 (ProductOption)
CREATE TABLE IF NOT EXISTS product_option (
    `sn` BIGINT AUTO_INCREMENT PRIMARY KEY,                     -- 옵션 SN
    `product_sn` BIGINT NOT NULL,                               -- 옵션 SN
    `name` VARCHAR(100) NOT NULL,                               -- 옵션 이름
    `additional_price` INT UNSIGNED NOT NULL,                   -- 옵션 추가 가격
    `is_delete` BOOLEAN NOT NULL DEFAULT FALSE,                 -- 삭제 여부
    INDEX idx_product_sn_is_delete (`product_sn`, `is_delete`), -- 상품Sn, 삭제 여부 인덱스
    CONSTRAINT FK_PRODUCT_OPTION_PRODUCT_SN FOREIGN KEY (product_sn) REFERENCES product(`sn`) ON DELETE CASCADE
);