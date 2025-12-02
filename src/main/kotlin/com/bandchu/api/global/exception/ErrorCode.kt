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

    // user
    USER_INVALID_CREDENTIAL("user-invalid-credential", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // Post
    POST_NOT_FOUND("post-not-found", HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    POST_INSERT_FAILED("post-insert-failed", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 생성에 실패했습니다."),
    POST_UPDATE_FAILED("post-update-failed", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 수정에 실패했습니다."),
    POST_DELETE_FAILED("post-delete-failed", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 삭제에 실패했습니다."),
    POST_TYPE_INVALID("post-type-invalid", HttpStatus.BAD_REQUEST, "유효하지 않은 게시판 타입입니다."),

    // Media
    MEDIA_UPLOAD_FAILED("media-upload-failed", HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다."),
    MEDIA_INSERT_FAILED("media-insert-failed", HttpStatus.INTERNAL_SERVER_ERROR, "미디어 정보 저장에 실패했습니다."),

    // comment
    COMMENT_NOT_FOUND("comment-not-found", HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    COMMENT_INSERT_FAILED("comment-insert-failed", HttpStatus.INTERNAL_SERVER_ERROR, "댓글 생성에 실패했습니다."),
    COMMENT_UPDATE_FAILED("comment-update-failed", HttpStatus.INTERNAL_SERVER_ERROR, "댓글 수정에 실패했습니다."),
    COMMENT_DELETE_FAILED("comment-delete-failed", HttpStatus.INTERNAL_SERVER_ERROR, "댓글 삭제에 실패했습니다.");

    val code: String
        get() = name
}
