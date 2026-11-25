-- =========================================================
-- v4: 검색 기록·재입고 알림·알림·추천 도서·독서 성향 테스트·출석 체크 (2026-11-25) — MySQL 8
-- 기존 데이터는 그대로 두고 표만 더해요. 여러 번 실행해도 괜찮아요.
-- (migrate-v3-mysql.sql을 먼저 실행한 DB에 실행)
-- =========================================================
USE bookmarket;

-- ===== v4 =====
-- 검색 기록 (최근 검색어·인기 검색어). 최근 검색어를 지우면 member_no만 비워서 인기 순위에는 남긴다
CREATE TABLE IF NOT EXISTS search_log (
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
CREATE TABLE IF NOT EXISTS restock_request (
  member_no    BIGINT      NOT NULL,
  book_id      VARCHAR(20) NOT NULL,
  requested_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no, book_id),
  CONSTRAINT fk_restock_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE,
  CONSTRAINT fk_restock_book   FOREIGN KEY (book_id)   REFERENCES book (book_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- 알림 (재입고·배송·쿠폰). read_at이 NULL이면 안 읽음
CREATE TABLE IF NOT EXISTS notification (
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
CREATE TABLE IF NOT EXISTS book_pick (
  pick_id    BIGINT       NOT NULL AUTO_INCREMENT,
  book_id    VARCHAR(20)  NOT NULL,
  comment    VARCHAR(200) NOT NULL,
  picked_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (pick_id),
  CONSTRAINT fk_pick_book FOREIGN KEY (book_id) REFERENCES book (book_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 곰 독서 성향 테스트 결과 (회원당 1개, 다시 하면 덮어씀)
CREATE TABLE IF NOT EXISTS taste_result (
  member_no BIGINT      NOT NULL,
  bear_type VARCHAR(20) NOT NULL,
  taken_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (member_no),
  CONSTRAINT fk_taste_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 출석 체크 (하루 1번)
CREATE TABLE IF NOT EXISTS attendance (
  member_no  BIGINT NOT NULL,
  check_date DATE   NOT NULL,
  PRIMARY KEY (member_no, check_date),
  CONSTRAINT fk_attend_member FOREIGN KEY (member_no) REFERENCES member (member_no) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT IGNORE INTO coupon VALUES
('TASTE500', '독서 성향 테스트 보상 500원', 'AMOUNT', 500, 0, NULL, 1),
('ATTEND7', '7일 출석 보상 1,000원', 'AMOUNT', 1000, 0, NULL, 1);
