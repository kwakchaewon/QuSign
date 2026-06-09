-- 전자서명법 제31조: 감사 로그 10년 보존 의무
-- retained_until 은 삽입 시 애플리케이션이 계산(created_at + 10년)하여 기록.
-- 이 컬럼은 UPDATE·DELETE 대상이 되어서는 안 됨.

ALTER TABLE audit_logs ADD COLUMN retained_until DATETIME(6) NULL;

UPDATE audit_logs SET retained_until = created_at + INTERVAL 10 YEAR;

ALTER TABLE audit_logs MODIFY COLUMN retained_until DATETIME(6) NOT NULL;

CREATE INDEX idx_audit_retained_until ON audit_logs (retained_until);
