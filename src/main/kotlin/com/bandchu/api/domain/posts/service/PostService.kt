package com.bandchu.api.domain.posts.service

import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.response.CreatePostResponse
import com.bandchu.api.domain.posts.dto.PostListItem
import com.bandchu.api.domain.posts.dto.request.UpdatePostRequest
import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.dto.response.CreateMediaResponse
import com.bandchu.api.domain.posts.dto.response.PostDetailResponse
import com.bandchu.api.domain.posts.dto.response.PostListResponse
import com.bandchu.api.domain.posts.table.PostType
import com.bandchu.api.domain.posts.repository.CommentRepository
import com.bandchu.api.domain.posts.repository.MediaRepository
import com.bandchu.api.domain.posts.repository.PostRepository
import com.bandchu.api.global.config.S3Uploader
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PostService(
    private val postRepository: PostRepository,
    private val mediaRepository: MediaRepository,
    private val commentRepository: CommentRepository,
    private val s3Uploader: S3Uploader
) {

    // 모든 게시판의 최신 글 1개씩 조회
    fun getAllPosts(): PostListResponse {
        val postTypes = listOf(
            PostType.FREE,
            PostType.MARKET,
            PostType.JOIN,
            PostType.REVIEW,
            PostType.ARTIST,
            PostType.DONGHAENG
        )

        val posts = postTypes.mapNotNull { type ->
            postRepository.findTopByPostTypeOrderByCreatedAtDesc(type)
        }

        if (posts.isEmpty()) {
            throw BusinessException(ErrorCode.POST_NOT_FOUND)
        }

        return PostListResponse(
            posts = posts.map {
                PostListItem(
                    postId = it.id,
                    postType = it.type.name,
                    title = it.title,
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            },
            totalElements = posts.size.toLong(),
            totalPages = 1
        )
    }

    // 특정 게시판 타입별 게시글 조회 (페이징)
    fun getPostByType(type: String, page: Int, size: Int): PostListResponse {
        val postType = try {
            PostType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BusinessException(ErrorCode.POST_TYPE_INVALID)
        }

        val posts = postRepository.findPostsByType(postType, page, size)
        val totalElements = postRepository.countPostsByType(postType)
        val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / size + 1).toInt()

        if (posts.isEmpty()) {
            throw BusinessException(ErrorCode.POST_NOT_FOUND)
        }

        return PostListResponse(
            posts = posts.map {
                PostListItem(
                    postId = it.id,
                    postType = it.type.name,
                    title = it.title,
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            },
            totalElements = totalElements,
            totalPages = totalPages
        )
    }

    // 게시글 생성
    fun createPost(memberId: Long, req: CreatePostRequest): CreatePostResponse {
        val post = postRepository.insertPost(
            type = req.postType,
            title = req.title,
            memberId = memberId,
            content = req.content,
        )

        return CreatePostResponse(
            id = post.id,
            memberId = post.memberId,
            title = post.title,
            content = post.content,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            type = post.type,
        )
    }

    // 게시글 상세 조회
    fun getPostDetail(postId: Long): PostDetailResponse {
        val post = postRepository.findById(postId)
            ?: throw BusinessException(ErrorCode.POST_NOT_FOUND)

        val mediaList = mediaRepository.findByPostId(postId)
        val comments = commentRepository.findByPostId(postId)

        return PostDetailResponse(
            postId = post.id,
            artistId = postRepository.findUserIdById(post.id),
            postType = post.type.name,
            title = post.title,
            content = post.content,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            media = mediaList.map {
                CreateMediaResponse(
                    mediaId = it.mediaId,
                    s3Url = it.s3Url,
                    fileSize = it.fileSize,
                    postId = post.id,
                    createdAt = post.createdAt
                )
            },
            comments = comments.map {
                CommentResponse(
                    postId = it.postId,
                    memberId = it.memberId,
                    commentId = it.commentId,
                    content = it.content,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            }
        )
    }

    // 미디어 업로드
    fun uploadMedia(postId: Long, file: MultipartFile): CreateMediaResponse {
        val s3Url = try {
            s3Uploader.upload(file, "posts/$postId")
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.MEDIA_UPLOAD_FAILED)
        }

        val fileSize = file.size
        val saved = mediaRepository.save(postId, s3Url, fileSize)

        return saved
    }

    // 게시글 업데이트
    fun updatePost(memberId: Long, postId: Long, req: UpdatePostRequest): PostDetailResponse {
        val post = postRepository.findById(postId)
            ?: throw BusinessException(ErrorCode.POST_NOT_FOUND)

        if (memberId != postRepository.findUserIdById(postId)) {
            throw BusinessException(ErrorCode.POST_FORBIDDEN)
        }

        return postRepository.updatePost(postId, req)
    }

    // 게시글 삭제
    fun deletePost(memberId: Long, postId: Long): Long {
        val post = postRepository.findById(postId)
            ?: throw BusinessException(ErrorCode.POST_NOT_FOUND)

        if (post.memberId != memberId)
            throw BusinessException(ErrorCode.POST_FORBIDDEN)

        val deletedRows = postRepository.deletePost(postId)
        if (deletedRows == 0L) {
            throw BusinessException(ErrorCode.POST_DELETE_FAILED)
        }

        return postId
    }
}

