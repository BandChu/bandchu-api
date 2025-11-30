package com.bandchu.api.domain.posts.controller

import com.bandchu.api.domain.posts.dto.request.CreateCommentRequest
import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.request.UpdatePostRequest
import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.dto.response.CreatedPostResponse
import com.bandchu.api.domain.posts.dto.response.LikeResponse
import com.bandchu.api.domain.posts.dto.response.MediaUploadResponse
import com.bandchu.api.domain.posts.dto.response.PostDetailResponse
import com.bandchu.api.domain.posts.dto.response.PostListResponse
import com.bandchu.api.domain.posts.dto.response.ReportResponse
import com.bandchu.api.domain.posts.model.Comment
import com.bandchu.api.domain.posts.service.PostService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("api/posts")
class PostController(
    private val postService: PostService
) {

    @GetMapping
    fun getAllPosts(

    ): ApiResponse<PostListResponse> {
        val result = postService.getallposts()
        return ApiResponse.success(
            data = result,
            message = "해당 회원이 볼 수 있는 모든 게시글을 보여줍니다."
        )
    }

    @GetMapping("/search/posttype")
    fun getPostsByType(
        @RequestParam(required = false) type: String,
        @RequestParam(required = false, defaultValue = "1") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
    ): ApiResponse<PostListResponse> {
        val result = postService.getpostbytype(
            type = type,
            page = page,
            size = size
        )
        return ApiResponse.success(
            data = result,
            message = "특정 게시판의 주어진 분량에 대한 글이 성공적으로 전송되었습니다."
        )
    }

    @PostMapping
    fun createPost(
        @RequestBody request: CreatePostRequest
    ): ApiResponse<CreatedPostResponse> {
        val result = postService.createpost(request)
        return ApiResponse.success(
           data = result,
            message = ""

        )
    }


    //TODO : 멤버 연결해서 조회하기
//
//    @GetMapping("/search/{postId}")
//    fun getPostDetail(
//        @PathVariable postId: Long
//    ): ApiResponse<PostDetailResponse> {
//
//        val result = postService.getPostDetail(postId)
//
//        return ApiResponse.success(
//            data = result,
//            message = "해당 게시글 상세 정보를 불러왔습니다."
//        )
//    }
//
//    @PatchMapping("/update/{postId}")
//    fun updatePost(
//        @PathVariable postId: Long,
//        @RequestBody request: UpdatePostRequest
//    ):ApiResponse<PostDetailResponse, String>  {
//
//val result = updatepost(postId, request)
//
//        return ApiResponse.success(
//            data = result,
//            message = "게시글이 성공적으로 업데이트되었습니다."
//        )
//    }
//
//    @DeleteMapping("/delete/{postId}")
//    fun deletePost(
//        @PathVariable postId: Long
//    ): ApiResponse<Map<String, Any>> {
//
//        val result = postService.deletepost(postId)
//
//        return ApiResponse.success(
//            data = result,
//            message = "게시글이 성공적으로 삭제되었습니다."
//        )
//    }
//    @PostMapping("/create/media/{postId}", consumes = ["multipart/form-data"])
//    fun uploadMedia(
//        @PathVariable postId: Long,
//        @RequestPart("file") file: MultipartFile
//    ): ApiResponse<MediaUploadResponse> {
//
//        val result = postService.uploadmedia(postId, file)
//
//        return ApiResponse.success(
//            data = result,
//            message = "미디어가 업로드되었습니다."
//        )
//
//
//    }
//@PostMapping("/posts/create/comments/{postId}")
//fun createComment(
//    @RequestBody request: CreateCommentRequest, bindingResult: BindingResult
//): ApiResponse<CommentResponse> {
//
//    val result = postService.createcomment(request)
//    return ApiResponse.success(
//        data = result,
//        message = "댓글이 작성되었습니다."
//    )
//}
//@DeleteMapping("/delete/comments/{postId}/{commentId}")
//fun deleteComment(
//    @PathVariable postId: Long,
//    @PathVariable commentId: Long
//): ApiResponse<CommentResponse> {
//
//    val result = postService.deletecomment(postId, commentId)
//    return ApiResponse.success(
//        data = result,
//        message = "게시글의 댓글이 삭제되었습니다."
//    )
//}
//@PostMapping("/create/likes/{postId}")
//fun createLike(
//    @PathVariable postId: Long,
//): ApiResponse<LikeResponse> {
//    val result = postService.createlike(postId)
//    return ApiResponse.success(
//        data = result,
//        message = "게시글에 좋아요가 추가되었습니다."
//    )
//
//}
//    @DeleteMapping("/delete/likes/{postId}/{likeId}}")
//    fun deleteLike(
//        @PathVariable likeId: Long,
//        @PathVariable postId: Long,
//    ): ApiResponse<LikeResponse> {
//        val result = postService.deletelike(postId, likeId)
//        return ApiResponse.success(
//            data = result,
//            message = "게시글의 좋아요를 취소하셨습니다."
//        )
//    }
//
//    @PostMapping("/create/reports/{postId}")
//    fun createReport(
//        @PathVariable postId: Long,
//    ): ApiResponse<ReportResponse>{
//        val result = postService.createreport(postId)
//        return ApiResponse.success(
//            data = result,
//            message = "게시글에 신고를 생성하셨습니다."
//        )
//    }
//
//
}
//
//
