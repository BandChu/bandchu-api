package com.bandchu.api.domain.posts.service

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.response.CreatePostResponse
import com.bandchu.api.domain.posts.dto.PostListItem
import com.bandchu.api.domain.posts.dto.PostWithMember
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
import com.bandchu.api.global.util.getCurrentUserId
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PostService(
    private val postRepository: PostRepository,
    private val mediaRepository: MediaRepository,
    private val commentRepository: CommentRepository,
    private val memberRepository: MemberRepository,
    private val s3Uploader: S3Uploader
) {

    fun getAllPosts(currentMemberId: Long?): PostListResponse {
        val postTypes = listOf(
            PostType.FREE,
            PostType.MARKET,
            PostType.JOIN,
            PostType.REVIEW,
            PostType.DONGHAENG,
            // PostType.ARTIST   // ARTIST는 구독한 아티스트 게시글로 별도 처리
        )

        val posts = postRepository.findLatestPostWithMemberByTypes(postTypes).toMutableList()

        // 구독한 아티스트 최신 게시글 1개 추가
        currentMemberId?.let { postRepository.findLatestSubArtistPost(it) }?.let {
            posts.add(it)
        }

        return PostListResponse(
            posts = posts.map {
                PostListItem(
                    postId = it.postId,
                    postType = it.postType.name,
                    memberId = it.memberId,
                    memberName = it.memberName,
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
    fun getPostByType(currentMemberId: Long?, type: String, page: Int, size: Int): PostListResponse {
        if (currentMemberId == null && type == PostType.ARTIST.name) {
            throw BusinessException(ErrorCode.POST_FORBIDDEN)
        }

        val postType = try {
            PostType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BusinessException(ErrorCode.POST_TYPE_INVALID)
        }

        val posts: List<PostWithMember> =
            postRepository.findPostsWithMemberByType(postType, page, size, currentMemberId)

        val totalElements = postRepository.countPostsByType(postType)
        val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / size + 1).toInt()

        return PostListResponse(
            posts = posts.map {
                PostListItem(
                    postId = it.postId,
                    postType = it.postType.name,
                    memberId = it.memberId,
                    memberName = it.memberName,
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

        val memberName = memberRepository.findMemberNameById(memberId)

        return CreatePostResponse(
            id = post.id,
            memberId = post.memberId,
            memberName = memberName,
            title = post.title,
            content = post.content,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            type = post.type,
        )
    }

    // 게시글 상세 조회
    fun getPostDetail(postId: Long): PostDetailResponse {
        val post: PostWithMember = postRepository.findPostWithMemberById(postId)
            ?: throw BusinessException(ErrorCode.POST_NOT_FOUND)

        val mediaList = mediaRepository.findByPostId(postId)
        val comments = commentRepository.findByPostIdWithMember(postId)

        return PostDetailResponse(
            postId = post.postId,
            memberId = post.memberId,
            memberName = post.memberName,
            postType = post.postType.name,
            title = post.title,
            content = post.content,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            media = mediaList.map {
                CreateMediaResponse(
                    mediaId = it.mediaId,
                    s3Url = it.s3Url,
                    fileSize = it.fileSize,
                    postId = postId,
                    createdAt = it.createdAt
                )
            },
            comments = comments.map {
                CommentResponse(
                    postId = it.postId,
                    memberId = it.memberId,
                    memberName = it.memberName,
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

        // ARTIST 게시판 수정 시 아티스트 검증
        if (post.type == PostType.ARTIST) {
            val isArtist = transaction {
                ArtiProfileTable
                    .selectAll()
                    .where { ArtiProfileTable.member eq memberId }
                    .empty()
                    .not()
            }

            if (!isArtist) {
                throw BusinessException(ErrorCode.NO_ARTIST_PERMISSION)
            }
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

