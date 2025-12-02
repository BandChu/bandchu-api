package com.bandchu.api.domain.posts.service

import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.repository.CommentRepository
import org.springframework.stereotype.Service

@Service
class CommentService(
    private val commentRepository: CommentRepository,
) {
    // 게시글의 모든 댓글 조회
    fun findAllByPostId(postId: Long): List<CommentResponse> {
        return commentRepository.findByPostId(postId)
    }
    
    // 댓글 작성
    fun insertComment(memberId: Long, postId: Long, content: String): CommentResponse {
        return commentRepository.insertComment(memberId, postId, content)
    }

    //댓글 삭제
    fun deleteComment(commentId: Long): Long {
//        if (commentRepository.findByCommentId(commentId) != null){
//            throw BusinessException(ErrorCode.)
//        }

        commentRepository.deleteComment(commentId)
        return commentId
    }

    //댓글 업데이트
    fun updateComment(commentId: Long, content: String): CommentResponse {
        //        if (commentRepository.findByCommentId(commentId) != null){
//            throw BusinessException(ErrorCode.)
//        }

        return commentRepository.updateComment(commentId, content)
    }
}