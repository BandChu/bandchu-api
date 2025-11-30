package com.bandchu.api.domain.member.table

import com.bandchu.api.domain.member.model.Role
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object MemberTable : LongIdTable("members") {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val nickname = varchar("nickname", 50)
    val role = enumerationByName("role", 20, Role::class)
    val googleId = varchar("google_id", 255).nullable().uniqueIndex()
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val createdAt = timestampWithTimeZone("created_at")
}
