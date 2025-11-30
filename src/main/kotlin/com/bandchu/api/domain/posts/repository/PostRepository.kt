package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.posts.model.Post
import com.bandchu.api.domain.posts.model.PostType
import com.bandchu.api.domain.posts.table.CommentTable.createdAt
import com.bandchu.api.domain.posts.table.PostTable
import com.bandchu.api.domain.posts.table.PostTable.post_type
import com.bandchu.api.domain.posts.table.PostTable.updatedAt
import com.bandchu.api.domain.posts.table.toPost
import com.nimbusds.openid.connect.sdk.assurance.evidences.attachment.Content
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import kotlin.time.Clock
import org.joda.time.DateTime
@Repository
class PostRepository {

    fun findTopByPostTypeOrderByCreatedAtDesc(type: PostType): Post? {
        return transaction {
            PostTable
                .selectAll()
                .where { PostTable.post_type eq type }
                .orderBy(PostTable.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.toPost()
        }
    }

    fun findPostsByType(type: PostType, page: Int, size: Int): List<Post> {
        val offset = (page -1) * size
        return transaction {
            PostTable
                .selectAll()
                .where{PostTable.post_type eq type}
                .orderBy(PostTable.createdAt, SortOrder.DESC)
                .toList()
                .drop(offset)
                .take(size)
                .map{it.toPost()}
        }
    }
    fun countPostsByType(type: PostType, page: Int, size: Int): Long {
        return transaction {
            PostTable
                .selectAll()
                .where{ PostTable.post_type eq type }
                .count()
        }
    }
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun insertPost(type: PostType,title: String, content: String): Post {

        return transaction{
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val insertedRow = PostTable.insert { row ->
                row[post_type] = type
                row[PostTable.title] = title
                row[PostTable.content] = content
                row[createdAt] = now
                row[updatedAt] = now  }

            val newId = insertedRow[PostTable.id]

            PostTable
                .selectAll()
                .single()
                .toPost()

        }
    }
    fun findById(id: Long): Post? = transaction {
        PostTable
            .selectAll()
            .where { PostTable.id eq  id.toInt() }
            .singleOrNull()
            ?.toPost()
    }


}