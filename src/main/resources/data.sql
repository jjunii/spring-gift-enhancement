INSERT INTO product (name, price, image_url, status)
VALUES ('상품1', 1000, 'https://example.com/image1.jpg', 'APPROVED'),
       ('상품2', 2000, 'https://example.com/image2.jpg', 'APPROVED'),
       ('상품3', 3000, 'https://example.com/image3.jpg', 'APPROVED'),
       ('상품4', 4000, 'https://example.com/image4.jpg', 'APPROVED'),
       ('상품5', 5000, 'https://example.com/image5.jpg', 'APPROVED'),
       ('카카오 상품', 6000, 'https://example.com/image6.jpg', 'PENDING_APPROVAL');

INSERT INTO option (name, quantity, product_id)
VALUES ('기본', 1, 1);
INSERT INTO option (name, quantity, product_id)
VALUES ('기본', 2, 2);
INSERT INTO option (name, quantity, product_id)
VALUES ('기본', 3, 3);
INSERT INTO option (name, quantity, product_id)
VALUES ('기본', 4, 4);
INSERT INTO option (name, quantity, product_id)
VALUES ('기본', 5, 5);
INSERT INTO option (name, quantity, product_id)
VALUES ('기본', 6, 6);