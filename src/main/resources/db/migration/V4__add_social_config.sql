-- 기존 members 테이블에 소셜 로그인용 컬럼 3개를 추가합니다.
ALTER TABLE members ADD COLUMN kakao_id VARCHAR(255) UNIQUE;
ALTER TABLE members ADD COLUMN naver_id VARCHAR(255) UNIQUE;
ALTER TABLE members ADD COLUMN provider VARCHAR(20) DEFAULT 'LOCAL';