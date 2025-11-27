package com.bandchu.api.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val docUri: String,
    val httpStatus: HttpStatus,
    val message: String,
) {
    /**
     * 공통 에러 타입 정의
     *
     * | 타입 | 설명 | HTTP 상태 코드 |
     * |------|------|----------------|
     * | Bad Request | 잘못된 요청 | 400 |
     * | Unauthorized | 인증되지 않은 요청 | 401 |
     * | Forbidden | 권한이 없는 요청 | 403 |
     * | Not Found | 요청한 리소스를 찾을 수 없음 | 404 |
     * | Conflict | 리소스 충돌 발생 | 409 |
     * | Internal Server Error | 서버 내부 오류 | 500 |
     */

    /* 도메인별 에러 코드 관리*/

    // member
    USER_EMAIL_DUPLICATED("user-email-duplicated", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USER_INVALID_CREDENTIAL("user-invalid-credential", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN("invalid-token", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN("invalid-refresh-token", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");

    val code: String
        get() = name
}