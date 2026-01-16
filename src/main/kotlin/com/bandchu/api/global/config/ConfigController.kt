package com.bandchu.api.global.config

import com.bandchu.api.global.response.ApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/config")
class ConfigController(
    @Value("\${oauth.google.client-id}")
    private val googleClientId: String
) {
    @GetMapping("/google-client-id")
    fun getGoogleClientId(): ResponseEntity<ApiResponse<GoogleClientIdResponse>> {
        val response = GoogleClientIdResponse(
            clientId = googleClientId
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "Google Client ID를 성공적으로 조회했습니다."))
    }
}

data class GoogleClientIdResponse(
    val clientId: String
)

