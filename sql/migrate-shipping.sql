-- =========================================================
-- 배송 단계 기능 추가용 (2026-11-18)
-- 이미 쓰고 있는 DB에 데이터를 지우지 않고 배송 시각 컬럼 2개를 더해요.
-- (상태 값 PREPARING·SHIPPING·DELIVERED는 기존 status 컬럼에 그대로 들어가요)
-- schema.sql / schema-postgres.sql을 새로 실행한 DB에는 이미 들어 있으니 실행하지 않아도 돼요.
--
-- Supabase: SQL Editor에 아래 두 줄을 붙여 넣고 Run
-- MySQL   : Workbench에서 USE bookmarket; 다음에 아래 두 줄 실행
-- =========================================================
ALTER TABLE orders ADD COLUMN shipped_at TIMESTAMP NULL;
ALTER TABLE orders ADD COLUMN delivered_at TIMESTAMP NULL;
