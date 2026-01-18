package com.bandchu.api.fixture

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.service.CommentService

class CommentFixture(
    private val commentService: CommentService,
    private val authFixture: AuthFixture
) {

    fun createComment(
        member: Member,
        postId: Long,
        content: String = "테스트 댓글"
    ): CommentResponse {

        authFixture.authenticateAs(member)

        return commentService.insertComment(
            // 테스트 코드에서 NULL 체크를 할 필요는 없어서 !!로 절대 NULL 아님을 보여줌
            memberId = member.id!!,
            postId = postId,
            content = content
        )
    }
}