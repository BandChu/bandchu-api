package com.bandchu.api.domain.member.service

import com.bandchu.api.domain.member.model.Member

data class SignupResult(
    val member: Member,
    val accessToken: String,
    val refreshToken: String
)

