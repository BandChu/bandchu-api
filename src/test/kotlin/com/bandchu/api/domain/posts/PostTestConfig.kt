package com.bandchu.api.domain.posts

import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.member.service.GoogleOAuthService
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.domain.posts.repository.CommentRepository
import com.bandchu.api.domain.posts.repository.MediaRepository
import com.bandchu.api.domain.posts.repository.PostRepository
import com.bandchu.api.domain.posts.service.CommentService
import com.bandchu.api.domain.posts.service.PostService
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.CommentFixture
import com.bandchu.api.fixture.PostFixture
import com.bandchu.api.global.config.S3Uploader
import com.bandchu.api.global.security.JwtService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean


@TestConfiguration
class PostTestConfig {

    @Bean
    fun authFixture(memberService: MemberService): AuthFixture {
        return AuthFixture(memberService)
    }


    @Bean
    open fun postService(
        postRepository: PostRepository,
        mediaRepository: MediaRepository,
        commentRepository: CommentRepository,
        memberRepository: MemberRepository,
        s3Uploader: S3Uploader
    ): PostService {
        return PostService(
            postRepository = postRepository,
            mediaRepository = mediaRepository,
            commentRepository = commentRepository,
            memberRepository = memberRepository,
            s3Uploader = s3Uploader
        )
    }
    @Bean
    fun memberRepository(): MemberRepository {
        return MemberRepository()
    }

    @Bean
    fun postRepository(): PostRepository {
        return PostRepository()
    }
    @Bean
    fun mediaRepository(): MediaRepository {
        return MediaRepository()
    }
    @Bean
    fun commentRepository(): CommentRepository {
        return CommentRepository()
    }
    @Bean
    fun s3Uploader(): S3Uploader {
        return mockk(relaxed = true)
    }


    @Bean
    fun commentService(
        commentRepository: CommentRepository,
        memberRepository: MemberRepository
    ): CommentService {
        return CommentService(
            commentRepository,
            memberRepository
        )
    }
    @Bean
    fun memberService(
        memberRepository: MemberRepository,
        jwtService: JwtService,
        googleOAuthService: GoogleOAuthService
    ): MemberService {
        return MemberService(
            memberRepository,
            jwtService,
            googleOAuthService
        )
    }

    @Bean
    fun postFixture(
        postService: PostService,
        authFixture: AuthFixture
    ): PostFixture {
        return PostFixture(postService, authFixture)
    }
    @Bean
    fun jwtService(): JwtService {
        return mockk(relaxed = true)
    }

    @Bean
    fun googleOAuthService(): GoogleOAuthService {
        return mockk(relaxed = true)
    }

    @Bean
    fun commentFixture(
        commentService: CommentService,
        authFixture: AuthFixture
    ): CommentFixture {
        return CommentFixture(commentService, authFixture)
    }
}