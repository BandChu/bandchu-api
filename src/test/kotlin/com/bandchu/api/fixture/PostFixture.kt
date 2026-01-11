package com.bandchu.api.fixture

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.response.CreatePostResponse
import com.bandchu.api.domain.posts.service.PostService
import com.bandchu.api.domain.posts.table.PostType


class PostFixture(
    private val postService: PostService,
    private val authFixture: AuthFixture
) {

    fun createPost(
        member: Member,
        title: String = "테스트 게시글",
        content: String = "본문입니다",
        postType: PostType = PostType.FREE
    ): CreatePostResponse {

        authFixture.authenticateAs(member)

        val req = CreatePostRequest(
            postType = postType,
            title = title,
            content = content
        )
// memberId Null일수 있지만 이 환경에선 절대 NULL 이 아니니까 !!로 안심시켜드림.
        return postService.createPost(member.id!!, req)
    }
}
