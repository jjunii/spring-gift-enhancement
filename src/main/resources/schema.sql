CREATE TABLE product
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    price     INT          NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    status    VARCHAR(30)   NOT NULL
);

CREATE TABLE member
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    email     VARCHAR(255)  NOT NULL UNIQUE,
    password  CHAR(60)      NOT NULL,
    role      VARCHAR(20)   NOT NULL
);

CREATE TABLE wish
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE (member_id, product_id)
);