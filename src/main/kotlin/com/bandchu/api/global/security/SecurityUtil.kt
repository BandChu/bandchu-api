package com.bandchu.api.global.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Component
object SecurityUtil {

    // 로그인한 사용자의 ID를 가져옴 (없으면 예외 발생)
    fun getCurrentMemberId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication.name == "anonymousUser") {
            throw RuntimeException("인증 정보가 없습니다.")
        }
        return authentication.name.toLong()
    }

    // 로그인 안 한 유저도 허용 (조회용 - 없으면 null 반환)
    fun getCurrentMemberIdOrNull(): Long? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication == null || authentication.name == "anonymousUser") {
            null
        } else {
            try {
                authentication.name.toLong()
            } catch (e: Exception) {
                null
            }
        }
    }
}