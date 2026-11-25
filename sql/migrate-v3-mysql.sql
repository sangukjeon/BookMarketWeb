-- =========================================================
-- 장르·재고·배송지·쿠폰함·장바구니 저장 추가 (2026-11-25) — 내 PC MySQL
-- 기존 데이터는 그대로 두고 칸과 표만 더해요.
-- Workbench: USE bookmarket; 다음에 이 파일 전체 실행
-- (첫 줄 ALTER는 이미 실행했다면 "Duplicate column" 오류가 나요 — 그 줄만 빼고 실행하면 돼요)
-- =========================================================
ALTER TABLE book ADD COLUMN stock INT NOT NULL DEFAULT 20;
UPDATE book SET category = 'IT/컴퓨터' WHERE category IN ('IT전문서', '컴퓨터입문');

CREATE TABLE IF NOT EXISTS member_address (
  address_id    BIGINT       NOT NULL AUTO_INCREMENT,
  member_no     BIGINT       NOT NULL,
  label         VARCHAR(20)  NOT NULL,
  receiver_name VARCHAR(50)  NOT NULL,
  phone         VARCHAR(20)  NOT NULL,
  address       VARCHAR(200) NOT NULL,
  is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (address_id),
  CONSTRAINT fk_addr_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS member_coupon (
  member_no     BIGINT      NOT NULL,
  code          VARCHAR(20) NOT NULL,
  registered_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no, code),
  CONSTRAINT fk_mc_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_mc_coupon FOREIGN KEY (code)      REFERENCES coupon (code)      ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cart_item (
  member_no  BIGINT      NOT NULL,
  book_id    VARCHAR(20) NOT NULL,
  quantity   INT         NOT NULL,
  updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no, book_id),
  CONSTRAINT fk_cart_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_cart_book   FOREIGN KEY (book_id)   REFERENCES book (book_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT IGNORE INTO member_coupon (member_no, code)
SELECT m.member_no, 'WELCOME10' FROM member m
WHERE EXISTS (SELECT 1 FROM coupon WHERE code = 'WELCOME10');
