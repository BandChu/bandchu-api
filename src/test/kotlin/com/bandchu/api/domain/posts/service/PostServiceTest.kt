package com.bandchu.api.domain.posts.service

import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.response.CreatePostResponse
import com.bandchu.api.domain.posts.repository.CommentRepository
import com.bandchu.api.domain.posts.repository.MediaRepository
import com.bandchu.api.domain.posts.repository.PostRepository
import com.bandchu.api.domain.posts.repository.ReportRepository
import com.bandchu.api.domain.posts.table.PostType
import com.bandchu.api.global.s3.S3Uploader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime

class PostServiceTest : StringSpec({

    val postRepository = mockk<PostRepository>()
    val mediaRepository = mockk<MediaRepository>()
    val reportRepository = mockk<ReportRepository>()
    val commentRepository = mockk<CommentRepository>()
    val s3Uploader = mockk<S3Uploader>()

    val postService = PostService(
        postRepository = postRepository,
        mediaRepository = mediaRepository,
        reportRepository = reportRepository,
        commentRepository = commentRepository,
        s3Uploader = s3Uploader
    )

    "getAllPosts should return latest posts per type" {
        // given
        val mockPost = CreatePostResponse(
            id = 1,
            memberId = 1,
            title = "Test Post",
            content = "Content",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            type = PostType.FREE
        )
        every { postRepository.findTopByPostTypeOrderByCreatedAtDesc(any()) } returns mockPost

        // when
        val result = postService.getAllPosts()

        // then
        result.posts.size shouldBe  6  // 6개의 타입
        result.posts.first().title shouldBe "Test Post"
    }

    "getPostByType should return paged posts" {
        val mockPost = CreatePostResponse(
            id = 1,
            memberId = 1,
            title = "Paged Post",
            content = "Content",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            type = PostType.FREE
        )

        every { postRepository.findPostsByType(PostType.FREE, 1, 10) } returns listOf(mockPost)
        every { postRepository.countPostsByType(PostType.FREE) } returns 1

        val result = postService.getPostByType("FREE", 1, 10)

        result.totalElements shouldBe 1
        result.totalPages shouldBe 1
        result.posts.first().title shouldBe "Paged Post"
    }

    "createPost should return created post response" {
        val req = CreatePostRequest(
            postType = PostType.FREE,
            title = "New Post",
            content = "New Content",
            memberId = 1
        )

        val savedPost = com.bandchu.api.domain.posts.dto.response.CreatePostResponse(
            id = 1,
            memberId = 1,
            title = "New Post",
            content = "New Content",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            type = PostType.FREE
        )

        every { postRepository.insertPost(PostType.FREE, "New Post", "New Content") } returns savedPost

        val result = postService.createPost(req)

        result.title shouldBe "New Post"
        result.content shouldBe "New Content"
        result.type shouldBe PostType.FREE
    }

})