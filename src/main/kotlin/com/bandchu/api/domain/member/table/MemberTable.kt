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
    val profileImageUrl = text("profile_image_url").nullable() // base64 데이터 URL 저장을 위해 TEXT로 변경
    val isProfileCompleted = bool("is_profile_completed").default(false)
    val createdAt = timestampWithTimeZone("created_at")
}
