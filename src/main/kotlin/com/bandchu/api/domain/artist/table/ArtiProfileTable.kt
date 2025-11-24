package com.bandchu.api.domain.artist.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object ArtiProfileTable : LongIdTable("arti_profiles") {
    val artistName = varchar("artist_name", 20)
    val genre = array<String>("genre")
    val description = text("description")
    val profileImageUrl = varchar("profile_image_url", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    val member = reference("members", MemberTable.id, onDelete = ReferenceOption.CASCADE)
}