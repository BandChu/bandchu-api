package com.bandchu.api.global.util

import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder

fun getCurrentUserId(): Long {
    val auth = SecurityContextHolder.getContext().authentication
        ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

    val principal = auth.principal
        ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

    return principal as? Long
        ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)
}

fun getCurrentUserRole(): Role {
    val auth = SecurityContextHolder.getContext().authentication
        ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

    val authority = auth.authorities
        .firstOrNull()?.authority
        ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

    val roleName = authority.removePrefix("ROLE_")

    return Role.valueOf(roleName)
}