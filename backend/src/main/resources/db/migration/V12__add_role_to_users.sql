ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'
        COMMENT '사용자 권한: USER | ADMIN';

-- 초기 관리자 계정 설정 방법
-- 방법 1 (SQL 직접): UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
-- 방법 2 (ENV):      ADMIN_EMAIL 환경변수를 설정하면 애플리케이션 시작 시 자동으로 ADMIN 권한 부여
