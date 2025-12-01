package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.dto.response.toCommentResponse
import com.bandchu.api.domain.posts.table.CommentTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class CommentRepository {

    fun findByPostId(postId: Long): List<CommentResponse> = transaction {
        CommentTable
            .selectAll()
            .where { CommentTable.postId eq postId.toLong() }
            .map { it.toCommentResponse () }
    }

    fun insertComment(postId: Long, content: String): CommentResponse = transaction {
        val now = OffsetDateTime.now()

        val inserted = CommentTable.insert { row ->
            row[CommentTable.postId] = postId
            row[CommentTable.content] = content
            row[CommentTable.createdAt] = now
            row[CommentTable.updatedAt] = now
        }

        val id = inserted[CommentTable.id]

        CommentTable
            .selectAll()
            .where { CommentTable.id eq id }
            .single()
            .toCommentResponse()
    }
}
