package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.posts.model.Comment
import com.bandchu.api.domain.posts.table.CommentTable
import kotlinx.datetime.toJavaLocalDateTime

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class CommentRepository {

    fun findByPostId(postId: Int): List<Comment> = transaction {
        CommentTable
            .selectAll()
            .where { CommentTable.postId eq postId.toLong() }
            .map { it.toComment() }
    }

    fun insertComment(postId: Long, content: String): Comment = transaction {
        val now = org.joda.time.DateTime.now()

        val inserted = CommentTable.insert { row ->
            row[CommentTable.postId] = postId
            row[CommentTable.content] = content
            // JODA 타입 넣는다
       //     row[CommentTable.createdAt] = now
         //   row[CommentTable.updatedAt] = now
        }

        val id = inserted[CommentTable.id]

        CommentTable
            .selectAll()
            .where { CommentTable.id eq id }
            .single()
            .toComment()
    }
}


// -----------------------------------------------
//  ResultRow → Comment 변환 (최종 버전)
// -----------------------------------------------
fun ResultRow.toComment(): Comment =
    Comment(
        comment_id = this[CommentTable.id],
        postId = this[CommentTable.postId],
        content = this[CommentTable.content],
        created_at = this[CommentTable.createdAt].toJavaLocalDateTime(),
        updated_at = this[CommentTable.updatedAt].toJavaLocalDateTime()
    )
