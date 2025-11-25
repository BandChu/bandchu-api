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
    // user
    USER_INVALID_CREDENTIAL("user-invalid-credential", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    
    // chat - 채팅 관련 에러 코드 추가
    CHATROOM_DUPLICATE_DIRECT("chatroom-duplicate-direct", HttpStatus.CONFLICT, "이미 두 사용자 간의 1:1 채팅방이 존재합니다."),
    CHATROOM_NOT_FOUND("chatroom-not-found", HttpStatus.NOT_FOUND, "해당 채팅방을 찾을 수 없습니다."),
    NOT_CHATROOM_MEMBER("not-chatroom-member", HttpStatus.FORBIDDEN, "해당 채팅방의 참여자가 아닙니다."),
    CHATROOM_INVALID_REQUEST("chatroom-invalid-request", HttpStatus.BAD_REQUEST, "잘못된 채팅방 요청입니다.");
    
    val code: String
        get() = name
}
