package com.bandchu.api.domain.member.repository

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.table.MemberTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class MemberRepository {

    fun save(member: Member): Member {
        return transaction {
            val insertResult = MemberTable.insert {
                it[email] = member.email
                it[password] = member.password
                it[nickname] = member.nickname
                it[role] = member.role
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
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

    private fun toMember(row: ResultRow): Member {
        return Member(
            id = row[MemberTable.id],
            email = row[MemberTable.email],
            password = row[MemberTable.password],
            nickname = row[MemberTable.nickname],
            role = row[MemberTable.role],
            createdAt = row[MemberTable.createdAt]
        )
    }
}

