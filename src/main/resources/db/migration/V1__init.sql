-- 1. 회원(Member) 테이블 생성
CREATE TABLE member (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- FAN, ARTIST 저장
    google_id VARCHAR(255),    -- OAuth용 Google ID
    profile_image_url TEXT,
    is_profile_completed BOOLEAN NOT NULL DEFAULT FALSE, -- 프로필 완료 여부
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. 구독(Subscription) 테이블 생성
CREATE TABLE subscription (
    id SERIAL PRIMARY KEY,
    -- 모델에는 생략되어 있지만, 실제 관계를 위해 외래키가 필요합니다
    subscriber_id BIGINT NOT NULL, -- 구독하는 사람
    artist_id BIGINT NOT NULL,     -- 구독 대상 (아티스트)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_subscriber FOREIGN KEY (subscriber_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist FOREIGN KEY (artist_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT unique_subscription UNIQUE (subscriber_id, artist_id)
);

-- 인덱스 추가 (조회 성능 향상)
CREATE INDEX idx_member_email ON member(email);
CREATE INDEX idx_member_google_id ON member(google_id);