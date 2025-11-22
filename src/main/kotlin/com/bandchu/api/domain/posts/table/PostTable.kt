package com.bandchu.api.domain.posts.table

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.member.table.MemberTable
import com.bandchu.api.domain.posts.model.PostType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object PostTable : Table("posts") {

    val id = long("post_id").autoIncrement()

    val post_type = enumerationByName("post_type", 20, PostType::class)

    val title = varchar("title", 20)

    val content = varchar("content", 100)

    val createdAt = datetime("created_at")

    val updatedAt = datetime("updated_at")

    val artistProfileId = long("artiprofile_id").references(ArtiProfileTable.id)

    val memberId = long("member_id")
        .references(MemberTable.id)

    override val primaryKey = PrimaryKey(id)
}