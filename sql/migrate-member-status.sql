-- =========================================================
-- 회원 관리 기능 추가용 (2026-11-18)
-- 이미 쓰고 있는 DB에 데이터를 지우지 않고 status 컬럼만 더해요.
-- schema.sql / schema-postgres.sql을 새로 실행한 DB에는 이미 들어 있으니 실행하지 않아도 돼요.
--
-- Supabase: SQL Editor에 아래 한 줄을 붙여 넣고 Run
-- MySQL   : Workbench에서 USE bookmarket; 다음에 아래 한 줄 실행
-- =========================================================
ALTER TABLE member ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE';
