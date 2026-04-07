package com.bandchu.api.domain.member.repository

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.table.MemberTable
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
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
                it[naverId] = member.naverId   // ✅ 추가
                it[kakaoId] = member.kakaoId   // ✅ 추가
                it[provider] = member.provider // ✅ 추가 (모델에 필드 있다면)
                it[profileImageUrl] = member.profileImageUrl
                it[isProfileCompleted] = member.isProfileCompleted
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
    }// 카카오 ID 업데이트
    fun updateKakaoId(memberId: Long, kakaoId: String): Member {
        return transaction {
            MemberTable.update({ MemberTable.id eq memberId }) {
                it[MemberTable.kakaoId] = kakaoId
            }
            findById(memberId)!!
        }
    }

    // 네이버 ID 업데이트
    fun updateNaverId(memberId: Long, naverId: String): Member {
        return transaction {
            MemberTable.update({ MemberTable.id eq memberId }) {
                it[MemberTable.naverId] = naverId
            }
            findById(memberId)!!
        }
    }

    // 소셜 ID로 회원 찾기 (linkOAuth 등에서 중복 체크용)
    fun findByKakaoId(kakaoId: String): Member? = transaction {
        MemberTable.selectAll().where { MemberTable.kakaoId eq kakaoId }.firstOrNull()?.let { toMember(it) }
    }

    fun findByNaverId(naverId: String): Member? = transaction {
        MemberTable.selectAll().where { MemberTable.naverId eq naverId }.firstOrNull()?.let { toMember(it) }
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
                it[MemberTable.isProfileCompleted] = true
            }
            
            MemberTable
                .selectAll()
                .where { MemberTable.id eq memberId }
                .single()
                .let { toMember(it) }
        }
    }

    fun updateRole(memberId: Long, role: com.bandchu.api.domain.member.model.Role): Member {
        return transaction {
            MemberTable.update({ MemberTable.id eq memberId }) {
                it[MemberTable.role] = role
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
            naverId = row[MemberTable.naverId],   // ✅ 추가
            kakaoId = row[MemberTable.kakaoId],   // ✅ 추가
            provider = row[MemberTable.provider], // ✅ 추가
            profileImageUrl = row[MemberTable.profileImageUrl],
            isProfileCompleted = row[MemberTable.isProfileCompleted],
            createdAt = localDateTime
        )
    }

    fun findMemberNameById(memberId: Long): String = transaction{
        MemberTable
            .selectAll()
            .where{ MemberTable.id eq memberId }
            .map { it[MemberTable.nickname] }
            .single()
    }
}

