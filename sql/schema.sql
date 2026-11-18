-- =========================================================
-- BookMarketWeb 데이터베이스 (MySQL 8.x)
-- 실행: MySQL Workbench에서 이 파일을 열고 전체 실행(번개 아이콘)
-- =========================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS bookmarket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 웹 애플리케이션 전용 계정 (db.properties와 같아야 해요)
CREATE USER IF NOT EXISTS 'bookmarket'@'localhost' IDENTIFIED BY 'bookmarket1234';
GRANT SELECT, INSERT, UPDATE, DELETE ON bookmarket.* TO 'bookmarket'@'localhost';
FLUSH PRIVILEGES;

USE bookmarket;

DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS wishlist;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS coupon;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS member;

-- 회원 (자체 가입 + 구글/네이버/카카오 소셜 + 관리자)
CREATE TABLE member (
  member_no     BIGINT       NOT NULL AUTO_INCREMENT,
  login_id      VARCHAR(50)  NOT NULL,
  password_hash VARCHAR(100) NULL COMMENT '소셜 회원은 비어 있음',
  salt          VARCHAR(50)  NULL,
  name          VARCHAR(50)  NOT NULL,
  phone         VARCHAR(20)  NULL     COMMENT '원본은 int였지만 0으로 시작하는 번호 때문에 문자열로 바꿈',
  address       VARCHAR(200) NULL,
  email         VARCHAR(100) NULL,
  provider      VARCHAR(10)  NOT NULL DEFAULT 'LOCAL' COMMENT 'LOCAL / GOOGLE / NAVER / KAKAO',
  provider_id   VARCHAR(100) NULL,
  role          VARCHAR(10)  NOT NULL DEFAULT 'USER'  COMMENT 'USER / ADMIN',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no),
  UNIQUE KEY uk_member_login (login_id),
  UNIQUE KEY uk_member_social (provider, provider_id)
) ENGINE=InnoDB;

-- 도서 (원본 book.txt의 7개 항목 + 표지·출판사·출처)
-- 카카오 책 검색 결과로 본 책은 자동으로 저장된다 (book_id = ISBN13)
CREATE TABLE book (
  book_id      VARCHAR(20)   NOT NULL COMMENT 'ISBN',
  name         VARCHAR(200)  NOT NULL,
  unit_price   INT           NOT NULL,
  author       VARCHAR(200)  NULL,
  description  VARCHAR(1000) NULL,
  category     VARCHAR(50)   NULL,
  release_date VARCHAR(10)   NULL,
  image        VARCHAR(300)  NULL     COMMENT '파일 이름 또는 http 주소',
  publisher    VARCHAR(100)  NULL,
  source       VARCHAR(10)   NOT NULL DEFAULT 'LOCAL' COMMENT 'LOCAL / KAKAO (예전 ALADIN)',
  PRIMARY KEY (book_id)
) ENGINE=InnoDB;

-- 쿠폰 (정률 RATE: value=% / 정액 AMOUNT: value=원)
CREATE TABLE coupon (
  code         VARCHAR(20)  NOT NULL,
  name         VARCHAR(50)  NOT NULL,
  type         VARCHAR(10)  NOT NULL COMMENT 'RATE / AMOUNT',
  value        INT          NOT NULL,
  min_amount   INT          NOT NULL DEFAULT 0,
  max_discount INT          NULL     COMMENT '정률 쿠폰의 최대 할인액',
  active       TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (code)
) ENGINE=InnoDB;

-- 주문 (영수증 머리)
CREATE TABLE orders (
  order_id       BIGINT       NOT NULL AUTO_INCREMENT,
  member_no      BIGINT       NOT NULL,
  receiver_name  VARCHAR(50)  NOT NULL,
  receiver_phone VARCHAR(20)  NOT NULL,
  address        VARCHAR(200) NOT NULL,
  item_total     INT          NOT NULL,
  discount       INT          NOT NULL DEFAULT 0,
  shipping_fee   INT          NOT NULL DEFAULT 0,
  pay_amount     INT          NOT NULL,
  coupon_code    VARCHAR(20)  NULL,
  status         VARCHAR(10)  NOT NULL DEFAULT 'ORDERED' COMMENT 'ORDERED / CANCELLED',
  ordered_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cancelled_at   DATETIME     NULL,
  PRIMARY KEY (order_id),
  KEY idx_orders_member (member_no),
  KEY idx_orders_status (status, ordered_at),
  CONSTRAINT fk_orders_member FOREIGN KEY (member_no) REFERENCES member (member_no),
  CONSTRAINT fk_orders_coupon FOREIGN KEY (coupon_code) REFERENCES coupon (code)
) ENGINE=InnoDB;

-- 주문 항목 (주문 당시 가격을 따로 저장)
CREATE TABLE order_item (
  order_id   BIGINT       NOT NULL,
  book_id    VARCHAR(20)  NOT NULL,
  book_name  VARCHAR(200) NOT NULL,
  unit_price INT          NOT NULL,
  quantity   INT          NOT NULL,
  line_total INT          NOT NULL,
  PRIMARY KEY (order_id, book_id),
  CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
  CONSTRAINT fk_item_book  FOREIGN KEY (book_id)  REFERENCES book (book_id)
) ENGINE=InnoDB;

-- 찜 (회원 N : M 도서)
CREATE TABLE wishlist (
  member_no  BIGINT      NOT NULL,
  book_id    VARCHAR(20) NOT NULL,
  created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no, book_id),
  CONSTRAINT fk_wish_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_wish_book   FOREIGN KEY (book_id)   REFERENCES book (book_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- 리뷰 (구매한 회원만, 책 1권에 1개)
CREATE TABLE review (
  review_id  BIGINT       NOT NULL AUTO_INCREMENT,
  member_no  BIGINT       NOT NULL,
  book_id    VARCHAR(20)  NOT NULL,
  rating     TINYINT      NOT NULL COMMENT '1~5',
  content    VARCHAR(300) NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (review_id),
  UNIQUE KEY uk_review_once (member_no, book_id),
  KEY idx_review_book (book_id),
  CONSTRAINT fk_review_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_review_book   FOREIGN KEY (book_id)   REFERENCES book (book_id)     ON DELETE CASCADE,
  CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

-- ---------------------------------------------------------
-- 처음 데이터
-- ---------------------------------------------------------

-- 원본 book.txt 3권
INSERT INTO book (book_id, name, unit_price, author, description, category, release_date, image, publisher) VALUES
('ISBN1234', '쉽게 배우는 JSP 웹 프로그래밍', 27000, '송미영', '단계별로 쇼핑몰을 구현하며 배우는 JSP 웹 프로그래밍', 'IT전문서', '2018/10/08', 'ISBN1234.jpg', '한빛아카데미'),
('ISBN1235', '안드로이드 프로그래밍', 33000, '우재남', '실습 단계별 명쾌한 멘토링!', 'IT전문서', '2022/01/22', 'ISBN1235.jpg', '한빛아카데미'),
('ISBN1236', '스크래치', 22000, '고광일', '컴퓨팅 사고력을 키우는 블록 코딩', '컴퓨터입문', '2019/06/10', 'ISBN1236.jpg', '한빛아카데미');

-- 관리자 계정: admin / admin1234 (SHA-256 + salt 10,000회 반복)
INSERT INTO member (login_id, password_hash, salt, name, phone, role)
VALUES ('admin', 'G4DmGwfEKjxayTYVq/2kdhyHoTrdwsXnqlMJVWTlmg4=', 'Qm9va01hcmtldEFkbWluIQ==', '관리자', '010-0000-0000', 'ADMIN');

-- 쿠폰 (회원마다 한 번씩 쓸 수 있고, 주문을 취소하면 다시 쓸 수 있다)
INSERT INTO coupon VALUES
('WELCOME10', '첫 주문 10% 할인', 'RATE', 10, 0, 5000, 1),
('BOOK3000', '3만 원 이상 3,000원 할인', 'AMOUNT', 3000, 30000, NULL, 1);
