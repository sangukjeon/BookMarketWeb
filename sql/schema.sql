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

DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS taste_result;
DROP TABLE IF EXISTS book_pick;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS restock_request;
DROP TABLE IF EXISTS search_log;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS member_coupon;
DROP TABLE IF EXISTS member_address;
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
  status        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / SUSPENDED(정지) / WITHDRAWN(탈퇴, 개인정보 지움)',
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
  category     VARCHAR(50)   NULL     COMMENT '장르: 소설, 시/에세이, 인문, 경제/경영, 자기계발, 과학, IT/컴퓨터, 어린이, 만화, 여행',
  release_date VARCHAR(10)   NULL,
  image        VARCHAR(300)  NULL     COMMENT '파일 이름 또는 http 주소',
  stock        INT           NOT NULL DEFAULT 20 COMMENT '재고 (0이면 품절)',
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
  status         VARCHAR(10)  NOT NULL DEFAULT 'ORDERED' COMMENT 'ORDERED → PREPARING → SHIPPING → DELIVERED / CANCELLED (취소는 PREPARING까지)',
  ordered_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cancelled_at   DATETIME     NULL,
  shipped_at     DATETIME     NULL COMMENT '배송 시작 시각',
  delivered_at   DATETIME     NULL COMMENT '배송 완료 시각',
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

-- 배송지 여러 개 (회원 1 : N, 최대 5개. 기본 배송지는 member.address에도 같이 저장)
CREATE TABLE member_address (
  address_id    BIGINT       NOT NULL AUTO_INCREMENT,
  member_no     BIGINT       NOT NULL,
  label         VARCHAR(20)  NOT NULL COMMENT '집, 회사 같은 이름',
  receiver_name VARCHAR(50)  NOT NULL,
  phone         VARCHAR(20)  NOT NULL,
  address       VARCHAR(200) NOT NULL,
  is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (address_id),
  CONSTRAINT fk_addr_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 회원 쿠폰함 (회원 N : M 쿠폰). 주문서에서는 여기 등록한 쿠폰만 쓸 수 있다
CREATE TABLE member_coupon (
  member_no     BIGINT      NOT NULL,
  code          VARCHAR(20) NOT NULL,
  registered_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no, code),
  CONSTRAINT fk_mc_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_mc_coupon FOREIGN KEY (code)      REFERENCES coupon (code)      ON DELETE CASCADE
) ENGINE=InnoDB;

-- 장바구니 저장 (로그인한 회원의 장바구니를 다른 PC에서도 이어서)
CREATE TABLE cart_item (
  member_no  BIGINT      NOT NULL,
  book_id    VARCHAR(20) NOT NULL,
  quantity   INT         NOT NULL,
  updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no, book_id),
  CONSTRAINT fk_cart_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_cart_book   FOREIGN KEY (book_id)   REFERENCES book (book_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===== v4 =====
-- 검색 기록 (최근 검색어·인기 검색어). 최근 검색어를 지우면 member_no만 비워서 인기 순위에는 남긴다
CREATE TABLE search_log (
  log_id       BIGINT       NOT NULL AUTO_INCREMENT,
  member_no    BIGINT       NULL,
  keyword      VARCHAR(100) NOT NULL,
  result_count INT          NOT NULL DEFAULT 0,
  searched_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (log_id),
  KEY idx_search_time (searched_at),
  KEY idx_search_member (member_no, searched_at),
  CONSTRAINT fk_search_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 재입고 알림 신청 (회원 N : M 도서). 알림을 보내면 지운다
CREATE TABLE restock_request (
  member_no    BIGINT      NOT NULL,
  book_id      VARCHAR(20) NOT NULL,
  requested_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no, book_id),
  CONSTRAINT fk_restock_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_restock_book   FOREIGN KEY (book_id)   REFERENCES book (book_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- 알림 (재입고·배송·쿠폰). read_at이 NULL이면 안 읽음
CREATE TABLE notification (
  notification_id BIGINT       NOT NULL AUTO_INCREMENT,
  member_no       BIGINT       NOT NULL,
  message         VARCHAR(200) NOT NULL,
  link            VARCHAR(200) NULL,
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at         DATETIME     NULL,
  PRIMARY KEY (notification_id),
  KEY idx_noti_member (member_no, read_at),
  CONSTRAINT fk_noti_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 윤상문고 추천 (관리자가 고른 책 + 한마디, 가장 최근 것이 메인에)
CREATE TABLE book_pick (
  pick_id    BIGINT       NOT NULL AUTO_INCREMENT,
  book_id    VARCHAR(20)  NOT NULL,
  comment    VARCHAR(200) NOT NULL,
  picked_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (pick_id),
  CONSTRAINT fk_pick_book FOREIGN KEY (book_id) REFERENCES book (book_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 곰 독서 성향 테스트 결과 (회원당 1개, 다시 하면 덮어씀)
CREATE TABLE taste_result (
  member_no BIGINT      NOT NULL,
  bear_type VARCHAR(20) NOT NULL,
  taken_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no),
  CONSTRAINT fk_taste_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 출석 체크 (하루 1번)
CREATE TABLE attendance (
  member_no  BIGINT NOT NULL,
  check_date DATE   NOT NULL,
  PRIMARY KEY (member_no, check_date),
  CONSTRAINT fk_attend_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE
) ENGINE=InnoDB;


-- ---------------------------------------------------------
-- 처음 데이터
-- ---------------------------------------------------------

-- 원본 book.txt 3권
INSERT INTO book (book_id, name, unit_price, author, description, category, release_date, image, publisher) VALUES
('ISBN1234', '쉽게 배우는 JSP 웹 프로그래밍', 27000, '송미영', '단계별로 쇼핑몰을 구현하며 배우는 JSP 웹 프로그래밍', 'IT/컴퓨터', '2018/10/08', 'ISBN1234.jpg', '한빛아카데미'),
('ISBN1235', '안드로이드 프로그래밍', 33000, '우재남', '실습 단계별 명쾌한 멘토링!', 'IT/컴퓨터', '2022/01/22', 'ISBN1235.jpg', '한빛아카데미'),
('ISBN1236', '스크래치', 22000, '고광일', '컴퓨팅 사고력을 키우는 블록 코딩', 'IT/컴퓨터', '2019/06/10', 'ISBN1236.jpg', '한빛아카데미');

-- 관리자 계정: admin / admin1234 (SHA-256 + salt 10,000회 반복)
INSERT INTO member (login_id, password_hash, salt, name, phone, role)
VALUES ('admin', 'G4DmGwfEKjxayTYVq/2kdhyHoTrdwsXnqlMJVWTlmg4=', 'Qm9va01hcmtldEFkbWluIQ==', '관리자', '010-0000-0000', 'ADMIN');

-- 쿠폰 (회원마다 한 번씩 쓸 수 있고, 주문을 취소하면 다시 쓸 수 있다)
INSERT INTO coupon VALUES
('WELCOME10', '첫 주문 10% 할인', 'RATE', 10, 0, 5000, 1),
('BOOK3000', '3만 원 이상 3,000원 할인', 'AMOUNT', 3000, 30000, NULL, 1),
('TASTE500', '독서 성향 테스트 보상 500원', 'AMOUNT', 500, 0, NULL, 1),
('ATTEND7', '7일 출석 보상 1,000원', 'AMOUNT', 1000, 0, NULL, 1);
