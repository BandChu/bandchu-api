package com.bandchu.api.domain.member.dto
data class KakaoUserInfo(
    val kakaoId: String,
    val email: String,
    val nickname: String? = null,
    val profileImage: String? = null
)