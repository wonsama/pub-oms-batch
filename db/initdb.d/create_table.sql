CREATE TABLE label_info (
    `id`        INT          NOT NULL AUTO_INCREMENT,
    `shop_code`  VARCHAR(32)  NOT NULL,
    `shop_name`  VARCHAR(64)  NOT NULL,
    `label_code`  VARCHAR(32) NOT NULL,
    `article_id`  VARCHAR(32)  ,
    `article_name`  VARCHAR(256)  ,
    `name`  VARCHAR(32)  ,
    `battery`  VARCHAR(32) ,
    `signals`  VARCHAR(32)  ,
    `type`  VARCHAR(32)  ,
    `template_type`  VARCHAR(32)  ,
    `template_manual`  VARCHAR(32) ,
    `status`  VARCHAR(32)  ,
    `update_status`  VARCHAR(32)  ,
    `last_response_time`  TIMESTAMP  ,
    `reg_date` VARCHAR(8) NOT NULL , /* yyyymmdd */

    UNIQUE KEY uk_label_info_1 (shop_code, label_code, reg_date), /* shop_code 는 검색속도 향상을 위함 */

    PRIMARY KEY (id)
);
