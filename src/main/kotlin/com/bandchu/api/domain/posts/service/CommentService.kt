package com.bandchu.api.domain.posts.service

import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.repository.CommentRepository
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val memberRepository: MemberRepository,
) {
    // 게시글의 모든 댓글 조회
    fun findAllByPostId(postId: Long): List<CommentResponse> {
        return commentRepository.findByPostId(postId)
            .map { comment ->
                comment.apply {
                    memberName = memberRepository.findMemberNameById(memberId)
                }
            }
    }
    
    // 댓글 작성
    fun insertComment(memberId: Long, postId: Long, content: String): CommentResponse {
        return commentRepository.insertComment(memberId, postId, content)
            .apply {
                memberName = memberRepository.findMemberNameById(memberId)
            }

    }

    //댓글 삭제
    fun deleteComment(memberId: Long, commentId: Long): Long {
        if (commentRepository.findByCommentId(commentId) == null){
                throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        //댓글 작성자가 아니면
        if ( commentRepository.findByCommentId(commentId)!!.memberId != memberId)
            throw BusinessException(ErrorCode.COMMENT_FORBIDDEN)

        commentRepository.deleteComment(commentId)
        return commentId
    }

    //댓글 업데이트
    fun updateComment(memberId: Long, commentId: Long, content: String): CommentResponse {
        if (commentRepository.findByCommentId(commentId) == null)
            throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)

        //댓글 작성자가 아니면
        if (commentRepository.findByCommentId(commentId)!!.memberId != memberId)
            throw BusinessException(ErrorCode.COMMENT_FORBIDDEN)

        return commentRepository.updateComment(commentId, content)
    }

}