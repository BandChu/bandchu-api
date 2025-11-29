package com.bandchu.api.domain.member.repository

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import com.bandchu.api.global.util.toKotlinLocalDateTime

@Repository
class MemberRepository {

    fun save(member: Member): Member {
        return transaction {
            val insertResult = MemberTable.insert {
                it[email] = member.email
                it[password] = member.password
                it[nickname] = member.nickname
                it[role] = member.role
                it[googleId] = member.googleId
                it[profileImageUrl] = member.profileImageUrl
                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            }

            MemberTable
                .selectAll()
                .where { MemberTable.id eq insertResult[MemberTable.id] }
                .single()
                .let { toMember(it) }
        }
    }

    fun existsByEmail(email: String): Boolean {
        return transaction {
            MemberTable
                .selectAll()
                .where { MemberTable.email eq email }
                .any()
        }
    }

    fun findByEmail(email: String): Member? {
        return transaction {
            MemberTable
                .selectAll()
                .where { MemberTable.email eq email }
                .firstOrNull()
                ?.let { toMember(it) }
        }
    }

    fun findById(id: Long): Member? {
        return transaction {
            MemberTable
                .selectAll()
                .where { MemberTable.id eq id }
                .firstOrNull()
                ?.let { toMember(it) }
        }
    }

    fun findByGoogleId(googleId: String): Member? {
        return transaction {
            MemberTable
                .selectAll()
                .where { MemberTable.googleId eq googleId }
                .firstOrNull()
                ?.let { toMember(it) }
        }
    }

    fun updateGoogleId(memberId: Long, googleId: String): Member {
        return transaction {
            MemberTable.update({ MemberTable.id eq memberId }) {
                it[MemberTable.googleId] = googleId
            }
            
            MemberTable
                .selectAll()
                .where { MemberTable.id eq memberId }
                .single()
                .let { toMember(it) }
        }
    }

    fun deleteById(id: Long) {
        transaction {
            MemberTable.deleteWhere { MemberTable.id eq id }
        }
    }

    fun updateProfile(memberId: Long, nickname: String, profileImageUrl: String?): Member {
        return transaction {
            MemberTable.update({ MemberTable.id eq memberId }) {
                it[MemberTable.nickname] = nickname
                it[MemberTable.profileImageUrl] = profileImageUrl
            }
            
            MemberTable
                .selectAll()
                .where { MemberTable.id eq memberId }
                .single()
                .let { toMember(it) }
        }
    }

    private fun toMember(row: ResultRow): Member {
        val offsetDateTime = row[MemberTable.createdAt]
        val localDateTime = offsetDateTime.toKotlinLocalDateTime()
        
        return Member(
            id = row[MemberTable.id].value,
            email = row[MemberTable.email],
            password = row[MemberTable.password],
            nickname = row[MemberTable.nickname],
            role = row[MemberTable.role],
            googleId = row[MemberTable.googleId],
            profileImageUrl = row[MemberTable.profileImageUrl],
            createdAt = localDateTime
        )
    }
}

