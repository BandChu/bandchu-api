package com.bandchu.api.domain.member.repository

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

@Repository
class MemberRepository {

    fun save(member: Member): Member {
        return transaction {
            val insertResult = MemberTable.insert {
                it[email] = member.email
                it[password] = member.password
                it[nickname] = member.nickname
                it[role] = member.role
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

    private fun toMember(row: ResultRow): Member {
        val offsetDateTime = row[MemberTable.createdAt]
        val javaLocalDateTime = offsetDateTime.toLocalDateTime()
        val month = Month.values()[javaLocalDateTime.monthValue - 1]
        val localDate = LocalDate(javaLocalDateTime.year, month, javaLocalDateTime.dayOfMonth)
        val localTime = LocalTime(
            javaLocalDateTime.hour,
            javaLocalDateTime.minute,
            javaLocalDateTime.second,
            javaLocalDateTime.nano
        )
        val localDateTime = LocalDateTime(localDate, localTime)
        
        return Member(
            id = row[MemberTable.id],
            email = row[MemberTable.email],
            password = row[MemberTable.password],
            nickname = row[MemberTable.nickname],
            role = row[MemberTable.role],
            createdAt = localDateTime
        )
    }
}

