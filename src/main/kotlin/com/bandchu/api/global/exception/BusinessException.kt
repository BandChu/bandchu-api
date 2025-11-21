package com.bandchu.api.global.exception

class BusinessException(
    val errorCode: ErrorCode,
) : RuntimeException() {
}