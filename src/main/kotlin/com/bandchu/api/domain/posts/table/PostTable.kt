package com.bandchu.api.domain.posts.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object PostTable : Table("posts") {

    val id = long("post_id").autoIncrement()

    val memberId = long("member_id")
        .references(MemberTable.id)

    val postType = enumerationByName("post_type", 20, PostType::class) // FREE, MARKET, JOIN, REVIEW, ARTIST

    val title = varchar("title", length = 20)
    val content = varchar("content", length = 100)

    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")


    override val primaryKey = PrimaryKey(id)
}
