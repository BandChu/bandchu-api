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
    INVALID_REFRESH_TOKEN("invalid-refresh-token", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    GOOGLE_AUTH_INVALID("google-auth-invalid", HttpStatus.UNAUTHORIZED, "구글 인증이 유효하지 않습니다."),
    OAUTH_TOKEN_INVALID("oauth-token-invalid", HttpStatus.UNAUTHORIZED, "소셜 인증 토큰이 유효하지 않습니다."),
    OAUTH_ALREADY_LINKED("oauth-already-linked", HttpStatus.CONFLICT, "이미 연결된 소셜 계정입니다."),
    INVALID_NICKNAME("invalid-nickname", HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다."),
    INVALID_EMAIL("invalid-email", HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD("invalid-password", HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다."),
    INVALID_INPUT("invalid-input", HttpStatus.BAD_REQUEST, "요청 데이터가 유효하지 않습니다."),

    // subscription
    SUBSCRIPTION_DUPLICATED("subscription-duplicated", HttpStatus.CONFLICT, "이미 구독 중인 아티스트입니다."),
    SUBSCRIPTION_NOT_FOUND("subscription-not-found", HttpStatus.NOT_FOUND, "구독 중이 아닌 아티스트입니다."),
    INVALID_ROLE("invalid-role", HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // artist
    ARTIST_NOT_FOUND("artist-not-found", HttpStatus.NOT_FOUND, "요청한 아티 프로필을 찾을 수 없습니다."),
    ARTIST_INSUFFICIENT_ROLE("insufficient-role", HttpStatus.FORBIDDEN, "회원 역할이 아티스트가 아닙니다."),
    ARTIST_FORBIDDEN("artist-forbidden", HttpStatus.FORBIDDEN, "해당 아티프로필에 대한 접근 권한이 없습니다."),

    ;

    val code: String
        get() = name
}