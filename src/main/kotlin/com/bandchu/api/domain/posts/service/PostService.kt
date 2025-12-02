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
import com.bandchu.api.domain.posts.repository.ReportRepository
import com.bandchu.api.global.config.S3Uploader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PostService(
    private val postRepository: PostRepository,
    private val mediaRepository: MediaRepository,
    private val reportRepository: ReportRepository,
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

        // 게시판 타입별 최신 글 조회
        val posts = postTypes.mapNotNull { type ->
            postRepository.findTopByPostTypeOrderByCreatedAtDesc(type)
        }

        return PostListResponse(
            posts = posts.map {
                PostListItem(
                    postId = it.id,            // PostResponse 필드 사용
                    postType = it.type.name,
                    title = it.title,
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            },
            totalElements = posts.size.toLong(),
            totalPages = 1 // TODO: 페이지네이션 구현 필요
        )
    }

    // 특정 게시판 타입별 게시글 조회 (페이징)
    fun getPostByType(type: String, page: Int, size: Int): PostListResponse {
        val postType = PostType.valueOf(type.uppercase())
        val posts = postRepository.findPostsByType(postType, page, size)
        val totalElements = postRepository.countPostsByType(postType)
        val totalPages = if (totalElements == 0L) 0
        else ((totalElements - 1) / size + 1).toInt()

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
    fun createPost(req: CreatePostRequest): CreatePostResponse {
        val post = postRepository.insertPost(
            type = req.postType,
            title = req.title,
            content = req.content,
            memberId = req.memberId
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
            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")

        val mediaList = mediaRepository.findByPostId(postId)
        val comments = commentRepository.findByPostId(postId)

        return PostDetailResponse(
            postId = post.id,
            artistId = postRepository.findUserIdById(post.id),
            postType = post.type,
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
                    createdAt =  post.createdAt
                )
            },
            comments = comments.map {
                CommentResponse(
                    commentId = it.commentId,
                    content = it.content,
                    createdAt = it.createdAt
                )
            }
        )
    }


    // 미디어 업로드
    fun uploadMedia(postId: Long, file: MultipartFile): CreateMediaResponse {
        // S3에 업로드
        val s3Url = s3Uploader.upload(file, "posts/$postId")
        val fileSize = file.size

        // Repository에 저장
        val saved = mediaRepository.save(postId, s3Url, fileSize)

        return saved
    }

    // 게시글 업데이트
    fun updatePost(postId: Long, req: UpdatePostRequest): PostDetailResponse {
            // 1. 게시글 존재 확인
            val post = postRepository.findById(postId)
                ?: throw NoSuchElementException("Post not found with id: $postId")

            // 2. 게시글 업데이트
            return postRepository.updatePost(postId, req)
    }


    fun deletePost(postId: Long) : Long{
        // 1. 게시글 존재 확인
        val post = postRepository.findById(postId)
            ?: throw NoSuchElementException("Post not found with id: $postId")

        // 2. 게시글 삭제
        return postRepository.deletePost(postId);
    }
}
