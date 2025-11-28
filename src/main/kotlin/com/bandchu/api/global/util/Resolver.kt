package com.bandchu.api.global.util

import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

fun getCurrentUserId(): Long {
    val auth = SecurityContextHolder.getContext().authentication
        ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

    val principal = auth.principal
        ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

    val id = when (principal) {
        is Long -> return principal
        is User -> principal.username
        else -> throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)
    }

    return id.toLongOrNull()
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