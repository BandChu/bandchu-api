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

    // ===== post =====
    POST_NOT_FOUND("post-not-found", HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    POST_FORBIDDEN("post-forbidden", HttpStatus.FORBIDDEN, "해당 게시글의 작성자가 아닙니다."),
    POST_INSERT_FAILED("post-insert-failed", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 생성에 실패했습니다."),
    POST_UPDATE_FAILED("post-update-failed", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 수정에 실패했습니다."),
    POST_DELETE_FAILED("post-delete-failed", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 삭제에 실패했습니다."),
    POST_TYPE_INVALID("post-type-invalid", HttpStatus.BAD_REQUEST, "유효하지 않은 게시판 타입입니다."),
    NO_ARTIST_PERMISSION("no-artist-permission", HttpStatus.FORBIDDEN, "아티스트가 아닌 사용자는 아티스트 게시판에 작성 불가능합니다."),

    // ===== media =====
    MEDIA_UPLOAD_FAILED("media-upload-failed", HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다."),
    MEDIA_INSERT_FAILED("media-insert-failed", HttpStatus.INTERNAL_SERVER_ERROR, "미디어 정보 저장에 실패했습니다."),

    // ===== comment =====
    COMMENT_NOT_FOUND("comment-not-found", HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN("cooment-forbidden", HttpStatus.FORBIDDEN, "해당 댓글의 사용자가 아닙니다."),
    COMMENT_INSERT_FAILED("comment-insert-failed", HttpStatus.INTERNAL_SERVER_ERROR, "댓글 생성에 실패했습니다."),
    COMMENT_UPDATE_FAILED("comment-update-failed", HttpStatus.INTERNAL_SERVER_ERROR, "댓글 수정에 실패했습니다."),
    COMMENT_DELETE_FAILED("comment-delete-failed", HttpStatus.INTERNAL_SERVER_ERROR, "댓글 삭제에 실패했습니다."),
  
    // ===== member =====
    USER_EMAIL_DUPLICATED("user-email-duplicated", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_TOKEN("invalid-token", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN("invalid-refresh-token", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    GOOGLE_AUTH_INVALID("google-auth-invalid", HttpStatus.UNAUTHORIZED, "구글 인증이 유효하지 않습니다."),
    OAUTH_TOKEN_INVALID("oauth-token-invalid", HttpStatus.UNAUTHORIZED, "소셜 인증 토큰이 유효하지 않습니다."),
    OAUTH_ALREADY_LINKED("oauth-already-linked", HttpStatus.CONFLICT, "이미 연결된 소셜 계정입니다."),
    INVALID_NICKNAME("invalid-nickname", HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다."),
    INVALID_EMAIL("invalid-email", HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD("invalid-password", HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다."),
    INVALID_INPUT("invalid-input", HttpStatus.BAD_REQUEST, "요청 데이터가 유효하지 않습니다."),

    // ===== chat =====
    CHATROOM_DUPLICATE_DIRECT("chatroom-duplicate-direct", HttpStatus.CONFLICT, "이미 두 사용자 간의 1:1 채팅방이 존재합니다."),
    CHATROOM_NOT_FOUND("chatroom-not-found", HttpStatus.NOT_FOUND, "해당 채팅방을 찾을 수 없습니다."),
    NOT_CHATROOM_MEMBER("not-chatroom-member", HttpStatus.FORBIDDEN, "해당 채팅방의 참여자가 아닙니다."),
    CHATROOM_INVALID_REQUEST("chatroom-invalid-request", HttpStatus.BAD_REQUEST, "잘못된 채팅방 요청입니다."),
    CHATROOM_CREATE_FAILED ("chatroom-created-failed", HttpStatus.PRECONDITION_FAILED, "채팅방 생성에 실패하였습니다."),

    // ===== subscription =====
    SUBSCRIPTION_DUPLICATED("subscription-duplicated", HttpStatus.CONFLICT, "이미 구독 중인 아티스트입니다."),
    SUBSCRIPTION_NOT_FOUND("subscription-not-found", HttpStatus.NOT_FOUND, "구독 중이 아닌 아티스트입니다."),
    SUBSCRIPTION_INSUFFICIENT_ROLE("subscription-insufficient-role", HttpStatus.FORBIDDEN, "팬만 구독할 수 있습니다."),

    // ===== artist =====
    ARTIST_NOT_FOUND("artist-not-found", HttpStatus.NOT_FOUND, "요청한 아티 프로필을 찾을 수 없습니다."),
    ARTIST_INSUFFICIENT_ROLE("insufficient-role", HttpStatus.FORBIDDEN, "회원 역할이 아티스트가 아닙니다."),
    ARTIST_FORBIDDEN("artist-forbidden", HttpStatus.FORBIDDEN, "해당 아티 프로필에 대한 접근 권한이 없습니다."),
    ARTIST_NOT_CREATED("artist-not-created", HttpStatus.CONFLICT, "아티스트가 아직 아티 프로필을 생성하지 않았습니다."),

    // ===== concert =====
    CONCERT_NOT_FOUND("concert-not-found", HttpStatus.NOT_FOUND, "요청한 콘서트를 찾을 수 없습니다."),
    CONCERT_FORBIDDEN("concert-not-found-or-forbidden", HttpStatus.FORBIDDEN, "해당 공연에 대한 접근 권한이 없습니다."),

    // album
    ALBUM_NOT_FOUND("album-not-found", HttpStatus.NOT_FOUND, "요청한 앨범을 찾을 수 없습니다."),

    // ===== friend =====
    FRIEND_SELF_REQUEST("friend-self-request", HttpStatus.BAD_REQUEST, "자기 자신에게는 친구 요청을 보낼 수 없습니다."),
    FRIEND_REQUEST_DUPLICATED("friend-request-duplicated", HttpStatus.CONFLICT, "이미 존재하는 친구 요청입니다."),
    FRIEND_REQUEST_ACCEPT_FAIL("friend-request-accept-fail", HttpStatus.BAD_REQUEST, "친구 요청을 수락할 수 없습니다."),
    FRIEND_REQUEST_REJECT_FAIL("friend-request-reject-fail", HttpStatus.BAD_REQUEST, "친구 요청을 거절할 수 없습니다.")

    ;

    val code: String
        get() = name
}
