package com.bandchu.api.domain.shorts.controller

import com.bandchu.api.domain.shorts.dto.CommentRequest
import com.bandchu.api.domain.shorts.dto.ShortsCommentResponse
import com.bandchu.api.domain.shorts.dto.ShortsResponse
import com.bandchu.api.domain.shorts.service.ShortsService
import com.bandchu.api.global.response.ApiResponse
import com.bandchu.api.global.security.SecurityUtil
import io.swagger.v3.oas.annotations.Operation

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/shorts")
class ShortsController(private val shortsService: ShortsService) {

    @Operation(summary = "릴스 피드 조회", description = "무한 스크롤을 위한 릴스 목록 조회 API")
    @GetMapping
    fun getFeed(
        @RequestParam(required = false) lastId: Long?,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<List<ShortsResponse>>> {
        val memberId = SecurityUtil.getCurrentMemberIdOrNull()
        val response = shortsService.getShortsFeed(lastId, size, memberId)
        return ResponseEntity.ok(ApiResponse.success(response, "피드 조회 성공"))
    }

    @Operation(summary = "릴스 업로드", description = "아티스트가 릴스 영상을 업로드합니다.")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @RequestPart file: MultipartFile,
        @RequestParam title: String,
        @RequestParam(required = false) description: String?
    ): ResponseEntity<ApiResponse<Long>> {
        val memberId = SecurityUtil.getCurrentMemberId()
        val shortsId = shortsService.upload(memberId, file, title, description)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(shortsId, "업로드 완료"))
    }

    @PostMapping("/{shortsId}/like")
    fun toggleLike(@PathVariable shortsId: Long): ResponseEntity<ApiResponse<Boolean>> {
        val memberId = SecurityUtil.getCurrentMemberId()
        val result = shortsService.toggleLike(shortsId, memberId)
        return ResponseEntity.ok(ApiResponse.success(result, if (result) "좋아요" else "좋아요 취소"))
    }

    @PostMapping("/{shortsId}/view")
    fun addViewCount(@PathVariable shortsId: Long): ResponseEntity<ApiResponse<Unit>> {
        shortsService.addViewCount(shortsId)
        return ResponseEntity.ok(ApiResponse.success(Unit, "조회수 증가"))
    }


    @Operation(summary = "릴스 댓글 조회")
    @GetMapping("/{shortsId}/comments")
    fun getComments(@PathVariable shortsId: Long): ResponseEntity<ApiResponse<List<ShortsCommentResponse>>> {
        val response = shortsService.getComments(shortsId)
        return ResponseEntity.ok(ApiResponse.success(response, "댓글 조회 성공"))
    }

    @Operation(summary = "릴스 댓글 작성")
    @PostMapping("/{shortsId}/comments")
    fun createComment(
        @PathVariable shortsId: Long,
        @RequestBody request: CommentRequest
    ): ResponseEntity<ApiResponse<Long>> {
        val memberId = SecurityUtil.getCurrentMemberId()
        val commentId = shortsService.createComment(shortsId, memberId, request.content)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(commentId, "댓글 작성 완료"))
    }

    @Operation(summary = "릴스 댓글 삭제")
    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(@PathVariable commentId: Long): ResponseEntity<ApiResponse<Unit>> {
        val memberId = SecurityUtil.getCurrentMemberId()
        shortsService.deleteComment(commentId, memberId)
        return ResponseEntity.ok(ApiResponse.success(Unit, "댓글 삭제 완료"))
    }

    @Operation(summary = "릴스 공유하기", description = "공유 횟수를 기록합니다.")
    @PostMapping("/{shortsId}/share")
    fun shareShorts(@PathVariable shortsId: Long): ResponseEntity<ApiResponse<Unit>> {
        shortsService.incrementShareCount(shortsId )
        return ResponseEntity.ok(ApiResponse.success(Unit, "공유 카운트 증가"))
    }
}