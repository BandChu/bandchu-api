package com.bandchu.api.domain.posts.controller

import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.request.UpdatePostRequest
import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.dto.response.CreateMediaResponse
import com.bandchu.api.domain.posts.dto.response.CreatePostResponse
import com.bandchu.api.domain.posts.dto.response.PostDetailResponse
import com.bandchu.api.domain.posts.dto.response.PostListResponse
import com.bandchu.api.domain.posts.service.CommentService
import com.bandchu.api.domain.posts.service.PostService
import com.bandchu.api.global.response.ApiResponse
import com.bandchu.api.global.util.getCurrentUserId
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService,
    private val commentService: CommentService,
) {

    // 모든 게시판의 최신 글 1개씩 조회
    @GetMapping
    fun getAllPosts(): ApiResponse<PostListResponse> {
        val result = postService.getAllPosts(getCurrentUserId())
        return ApiResponse.success(
            data = result,
            message = "해당 회원이 볼 수 있는 모든 게시글을 보여줍니다."
        )
    }

    // 특정 게시판 타입별 게시글 조회 (페이징)
    @GetMapping("/postType")
    fun getPostsByType(
        @RequestParam(required = true) type: String,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
    ): ApiResponse<PostListResponse> {
        val result = postService.getPostByType(
            type = type,
            page = page,
            size = size
        )
        return ApiResponse.success(
            data = result,
            message = "특정 게시판의 주어진 분량에 대한 글이 성공적으로 전송되었습니다."
        )
    }

    // 게시글 생성
    @PostMapping
    fun createPost(
        @RequestBody request: CreatePostRequest,
    ): ApiResponse<CreatePostResponse> {
        val memberId = getCurrentUserId()
        val result = postService.createPost(memberId, request) // Service 메서드명에 맞춤
        return ApiResponse.success(
            data = result,
            message = "게시글이 성공적으로 생성되었습니다."
        )
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    fun getPostDetail(
        @PathVariable postId: Long
    ): ApiResponse<PostDetailResponse> {
        val result = postService.getPostDetail(postId)
        return ApiResponse.success(
            data = result,
            message = "해당 게시글 상세 정보를 불러왔습니다."
        )
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestBody request: UpdatePostRequest,
    ): ApiResponse<PostDetailResponse> {
        val memberId = getCurrentUserId()
        val result = postService.updatePost(memberId, postId, request)
        return ApiResponse.success(
            data = result,
            message = "게시글이 성공적으로 업데이트되었습니다."
        )
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
    ): ApiResponse<Long> {
        val memberId = getCurrentUserId()
        val result = postService.deletePost(memberId, postId)
        return ApiResponse.success(
            data = result,
            message = "게시글이 성공적으로 삭제되었습니다."
        )
    }

    // 미디어 업로드
    @PostMapping("/create/media/{postId}", consumes = ["multipart/form-data"])
    fun uploadMedia(
        @PathVariable postId: Long,
        @RequestPart("file") file: MultipartFile
    ): ApiResponse<CreateMediaResponse> {
        val result = postService.uploadMedia(postId, file)
        return ApiResponse.success(
            data = result,
            message = "미디어가 업로드되었습니다."
        )
    }

    // 댓글 생성
    @PostMapping("/comments")
    fun createComment(
        @RequestParam postId: Long,
        @RequestBody content: String,
    ): ApiResponse<CommentResponse> {
        val memberId = getCurrentUserId()
        val result = commentService.insertComment(memberId, postId, content)
        return ApiResponse.success(
            data = result,
            message = "댓글이 작성되었습니다."
        )
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
    ): ApiResponse<Long> {
        val memberId = getCurrentUserId()
        val result = commentService.deleteComment(memberId, commentId)
        return ApiResponse.success(
            data = result,
            message = "게시글의 댓글이 삭제되었습니다."
        )
    }
    
    //댓글 수정
    @PutMapping("/comments/{commentId}")
    fun updateComment(
        @RequestBody content: String,
        @PathVariable commentId: Long,
    ): ApiResponse<CommentResponse> {
        val memberId = getCurrentUserId()
        val result = commentService.updateComment(memberId, commentId, content)
        return ApiResponse.success(
            data = result,
            message = "댓글이 작성되었습니다."
        )
    }

    /*
    // 신고 생성
    @PostMapping("/create/reports/{postId}")
    fun createReport(
        @PathVariable postId: Long
    ): ApiResponse<ReportResponse> {
        val result = postService.createReport(postId)
        return ApiResponse.success(
            data = result,
            message = "게시글에 신고가 생성되었습니다."
        )
    }

    // 좋아요 생성
    @PostMapping("/create/likes/{postId}")
    fun createLike(
        @PathVariable postId: Long
    ): ApiResponse<LikeResponse> {
        val result = postService.createLike(postId)
        return ApiResponse.success(
            data = result,
            message = "게시글에 좋아요가 추가되었습니다."
        )
    }

    // 좋아요 삭제
    @DeleteMapping("/delete/likes/{postId}/{likeId}")
    fun deleteLike(
        @PathVariable postId: Long,
        @PathVariable likeId: Long
    ): ApiResponse<LikeResponse> {
        val result = postService.deleteLike(postId, likeId)
        return ApiResponse.success(
            data = result,
            message = "게시글의 좋아요를 취소하셨습니다."
        )
    }
    */
}
