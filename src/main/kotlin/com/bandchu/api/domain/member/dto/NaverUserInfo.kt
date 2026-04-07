package com.bandchu.api.domain.member.dto

data class NaverUserInfo(
    val naverId: String,
    val email: String,
    val nickname: String? = null,
    val profileImage: String? = null
)