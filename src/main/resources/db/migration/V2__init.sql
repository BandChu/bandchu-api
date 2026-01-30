-- 1. 앨범(Album) 테이블 생성
CREATE TABLE album (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    release_date TIMESTAMP WITH TIME ZONE NOT NULL,
    cover_image_url TEXT,
    description TEXT,
    artist_profile_id BIGINT NOT NULL, -- 아티스트 회원 ID 연결
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Member 테이블의 아티스트와 외래키 연결
    CONSTRAINT fk_album_artist FOREIGN KEY (artist_profile_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 2. 트랙(Track) 테이블 생성
CREATE TABLE track (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    album_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Album 테이블과 외래키 연결
    CONSTRAINT fk_track_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

-- 인덱스 추가 (조회 성능 향상)
CREATE INDEX idx_album_artist ON album(artist_profile_id);
CREATE INDEX idx_track_album ON track(album_id);