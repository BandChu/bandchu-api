package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.posts.dto.request.UpdatePostRequest
import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.dto.response.CreateMediaResponse
import com.bandchu.api.domain.posts.dto.response.CreatePostResponse
import com.bandchu.api.domain.posts.dto.response.PostDetailResponse
import com.bandchu.api.domain.posts.dto.response.toPostDetailResponse
import com.bandchu.api.domain.posts.dto.response.toPostResponse
import com.bandchu.api.domain.posts.table.CommentTable
import com.bandchu.api.domain.posts.table.MediaTable
import com.bandchu.api.domain.posts.table.PostTable
import com.bandchu.api.domain.posts.table.PostType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class PostRepository {

    // 가장 최근 게시글 조회
    fun findTopByPostTypeOrderByCreatedAtDesc(type: PostType): CreatePostResponse? {
        return transaction {
            PostTable
                .selectAll()
                .where { PostTable.postType eq type }
                .orderBy(PostTable.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.toPostResponse()
        }
    }

    // 타입별 게시글 페이지 조회
    fun findPostsByType(type: PostType, page: Int, size: Int): List<CreatePostResponse> {
        val offset = (page - 1) * size
        return transaction {
            PostTable
                .selectAll()
                .where { PostTable.postType eq type }
                .orderBy(PostTable.createdAt, SortOrder.DESC)
                .toList()
                .drop(offset)       // 페이지 시작 위치
                .take(size)         // 페이지 크기만큼 가져오기
                .map { it.toPostResponse() }
        }
    }

    // 타입별 게시글 수 카운트
    fun countPostsByType(type: PostType): Long {
        return transaction {
            PostTable
                .selectAll()
                .where { PostTable.postType eq type }
                .count()
        }
    }

    // 게시글 삽입 후 반환
    fun insertPost(memberId: Long, type: PostType, title: String, content: String): CreatePostResponse {
        return transaction {
            val now = OffsetDateTime.now()
            val insertedRow = PostTable.insert { row ->
                row[PostTable.postType] = type
                row[PostTable.title] = title
                row[PostTable.content] = content
                row[PostTable.createdAt] = now
                row[PostTable.updatedAt] = now
                row[PostTable.memberId] = memberId
            }

            PostTable
                .selectAll()
                .where { PostTable.id eq insertedRow[PostTable.id] }
                .single()
                .toPostResponse()
        }
    }

    // ID로 게시글 조회
    fun findById(postId: Long): CreatePostResponse? = transaction {
        PostTable
            .selectAll()
            .where { PostTable.id eq postId }
            .singleOrNull()
            ?.toPostResponse()
    }

    //게시글 ID로 유저 ID 조회
    fun findUserIdById(postId: Long): Long = transaction {
        PostTable
            .selectAll()
            .where(PostTable.id eq postId)
            .single()[PostTable.memberId]
    }

    fun updatePost(id: Long, req: UpdatePostRequest): PostDetailResponse =
        transaction {

            // 1. 게시글 존재 여부 확인
            val existing = PostTable
                .selectAll()
                .where { PostTable.id eq id }
                .singleOrNull()
                ?: throw IllegalArgumentException("게시글이 존재하지 않습니다: $id")

            // 2. 게시글 업데이트
            PostTable.update({ PostTable.id eq id }) {
                it[title] = req.title
                it[content] = req.content
                it[updatedAt] = OffsetDateTime.now()
            }

            // 3. 미디어 리스트 조회
            val mediaList = MediaTable
                .selectAll()
                .where { MediaTable.postId eq id }
                .map { row ->
                    CreateMediaResponse(
                        postId = id,
                        mediaId = row[MediaTable.id],
                        s3Url = row[MediaTable.s3Url],
                        fileSize = row[MediaTable.s3FileSize],
                        createdAt = OffsetDateTime.now(),
                    )
                }

            // 4. 댓글 조회
            val comments = CommentTable
                .selectAll()
                .where { CommentTable.postId eq id }
                .map { row ->
                    CommentResponse(
                        commentId = row[CommentTable.id],
                        content = row[CommentTable.content],
                        createdAt = row[CommentTable.createdAt]
                    )
                }

            // 5. 수정된 게시글 다시 조회 후 DTO 생성
            val updatedRow = PostTable
                .selectAll()
                .where { PostTable.id eq id }
                .single()

            updatedRow.toPostDetailResponse(
                media = mediaList,
                comments = comments
            )
        }

    fun deletePost(postId: Long) :Long {
        transaction {
            PostTable
                .deleteWhere { PostTable.id eq postId }
        }
        return postId;
    }
}
