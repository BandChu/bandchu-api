-- 1. 아티스트 프로필 (ArtiProfile)
CREATE TABLE artist_profile (
    id BIGSERIAL PRIMARY KEY,
    artist_name VARCHAR(100) NOT NULL,
    description TEXT,
    profile_image_url TEXT,
    member_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artist_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 아티스트 장르 (Enum List 대응)
CREATE TABLE artist_genre (
    id BIGSERIAL PRIMARY KEY,
    artist_profile_id BIGINT NOT NULL,
    genre VARCHAR(20) NOT NULL, -- BALLAD, ROCK 등
    CONSTRAINT fk_genre_artist FOREIGN KEY (artist_profile_id) REFERENCES artist_profile(id) ON DELETE CASCADE
);

-- 2. 콘서트 (Concert)
CREATE TABLE concert (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    place VARCHAR(255) NOT NULL,
    poster_image_url TEXT,
    information TEXT,
    booking_url TEXT,
    booking_schedule TIMESTAMP WITH TIME ZONE,
    artist_profile_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_concert_artist FOREIGN KEY (artist_profile_id) REFERENCES artist_profile(id) ON DELETE CASCADE
);

-- 콘서트 일정 (ConcertSchedule)
CREATE TABLE concert_schedule (
    id BIGSERIAL PRIMARY KEY,
    concert_id BIGINT NOT NULL,
    concert_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_schedule_concert FOREIGN KEY (concert_id) REFERENCES concert(id) ON DELETE CASCADE
);

-- 3. 채팅 (Chat)
CREATE TABLE chat_room (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    room_type VARCHAR(20) NOT NULL, -- DIRECT, GROUP
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE member_chat_room (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    last_read_message_id BIGINT,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room FOREIGN KEY (room_id) REFERENCES chat_room(id) ON DELETE CASCADE
);

-- 4. 친구 (Friend)
CREATE TABLE friends (
    friend_id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(10) DEFAULT 'PENDING', -- PENDING, ACCEPTED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_friend_sender FOREIGN KEY (sender_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_receiver FOREIGN KEY (receiver_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT unique_friend_request UNIQUE (sender_id, receiver_id)
);

-- 5. 게시글 및 댓글 (Posts)
CREATE TABLE posts (
    post_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    post_type VARCHAR(20) NOT NULL, -- FREE, MARKET, ARTIST 등
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

CREATE TABLE comment (
    comment_id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

CREATE TABLE media (
    media_id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    s3_url VARCHAR(500) NOT NULL,
    s3_file_size BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_media_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE
);

CREATE TABLE report (
    report_id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    report_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE
);