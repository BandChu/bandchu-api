package com.bandchu.api.domain.posts.service

import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.response.CreatedPostResponse
import com.bandchu.api.domain.posts.dto.response.MediaUploadResponse
import com.bandchu.api.domain.posts.dto.response.PostListItem
import com.bandchu.api.domain.posts.dto.response.PostListResponse
import com.bandchu.api.domain.posts.model.Media
import com.bandchu.api.domain.posts.model.PostType
import com.bandchu.api.domain.posts.repository.CommentRepository
import com.bandchu.api.domain.posts.repository.MediaRepository
import com.bandchu.api.domain.posts.repository.PostRepository
import com.bandchu.api.domain.posts.repository.ReportRepository
import com.bandchu.api.global.s3.S3Uploader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.collections.map

@Service
class PostService(
    private val postRepository: PostRepository,
    private val mediaRepository: MediaRepository,
    private val reportRepository: ReportRepository,
    private val commentRepository: CommentRepository,

    private val s3Uploader: S3Uploader
) {

    fun getallposts(): PostListResponse {

        // 구독이 있으면 구독한 사람들의 리스트까지 구독이 없으면 디폴트로는 이런식으로 넘기기
        val postTypes =
            listOf(  PostType.FREE,
                PostType.MARKET,
                PostType.JOIN,
                PostType.REVIEW,
                PostType.ARTIST,
                PostType.DONGHAENG)

        // 게시판 타입별 최신 1개 조회
        val posts = postTypes.mapNotNull { type ->
            postRepository.findTopByPostTypeOrderByCreatedAtDesc(type)
        }

        return PostListResponse(
            posts = posts.map {
                PostListItem(
                    postId = it.post_id!!,
                    postType = it.postType.name,
                    title = it.title,
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            },
            totalElements = posts.size.toLong(),
            totalPages = 1 // TODO: 나중에 페이지네이션 필요할때 숫자 바꾸기
        )
    }

    fun getpostbytype(type: String, page: Int, size: Int): PostListResponse {
        val postType = PostType.valueOf(type.uppercase())
        val posts = postRepository.findPostsByType(postType,page,size)
        val totalElements = postRepository.countPostsByType(postType,page,size)
        val totalPages = if(totalElements == 0L) 0
        else((totalElements -1) / size+1).toInt()

        return PostListResponse(
            posts = posts.map {
                PostListItem(
                 postId = it.post_id!!,
                    postType = it.postType.name,
                    title = it.title,
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            },
            totalElements = totalElements,
            totalPages = totalPages
        )

    }

    fun createpost(req: CreatePostRequest): CreatedPostResponse{

        val post = postRepository.insertPost(
            type = req.postType,
            title = req.title,
            content = req.content
        )
        return CreatedPostResponse(
            postId = post.post_id!!,
            title = post.title,
            createdAt = post.createdAt.toString(),
            updatedAt = post.updatedAt.toString(),
            postType = post.postType.name,
            content = post.content
        )
    }


//    fun getPostDetail(postId: Long): PostDetailResponse {
//
//        val post = postRepository.findById(postId)
//            ?: throw IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
//
//        val mediaList = mediaRepository.findByPostId(postId)
//        val comments = commentRepository.findByPostId(postId)
//
//        return PostDetailResponse(
//            postId = post.post_id!!.toLong(),
//            artistId =  post.memberId,          // 너의 스키마에 맞게 수정 가능
//            postType = post.postType.name,
//            title = post.title,
//            content = post.content,
//            createdAt = post.createdAt.toString(),
//            updatedAt = post.updatedAt.toString(),
//            media = mediaList.map {
//                MediaItem(
//                    mediaId = it.mediaId,
//                    s3Url = it.s3Url,
//                    fileSize = it.fileSize.toString()
//                )
//            },
//            comments = comments.map {
//                CommentItem(
//                    commentId = it.commentId,
//                    content = it.content,
//                    createdAt = it.createdAt.toString()
//                )
//            }
//        )
//    }

fun uploadMedia(postId: Long, file: MultipartFile): MediaUploadResponse {

    val s3Url = s3Uploader.upload(file, "posts/$postId")

    val saved = mediaRepository.save(
        Media(
            postId = postId,
            artistId = getArtistIdByPostId(postId),
            s3Url = s3Url,
            fileSize = file.size
            )

    )
    return MediaUploadResponse(
        mediaId = saved.id!!,
        postId = saved.postId,
        artistId = saved.artistId,
        s3Url = saved.s3Url,
        fileSize = saved.fileSize.toString()
    )
}


}