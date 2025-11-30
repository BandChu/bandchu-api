-- 테스트용 회원 데이터
INSERT INTO members (id, email, password, nickname, role, created_at) VALUES
(1, 'test1@test.com', 'pass', 'TestUser1', 'FAN', NOW()),
(2, 'test2@test.com', 'pass', 'TestUser2', 'FAN', NOW()),
(3, 'test3@test.com', 'pass', 'TestUser3', 'FAN', NOW()),
(999, 'test999@test.com', 'pass', 'TestUser999', 'FAN', NOW())
ON CONFLICT (id) DO NOTHING;
