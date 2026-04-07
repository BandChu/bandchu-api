package com.bandchu.api.domain.posts.table


// 좋아요 객체를 일일이 만들면 테이블이 너무 많아져 좋아요 객체 자체를 타입으로 받아서 LikeTable에서 관리하는 식으로 만들었습니다.
enum class LikeTargetType {
    POST, COMMENT, ARTIST, CONCERT
}