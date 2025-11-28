package com.bandchu.api.domain.subscription.repository

import com.bandchu.api.domain.subscription.model.Subscription
import com.bandchu.api.domain.subscription.table.SubscriptionTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
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
class SubscriptionRepository {

    fun save(subscription: Subscription): Subscription {
        return transaction {
            val insertResult = SubscriptionTable.insert {
                it[memberId] = subscription.memberId
                it[artProfileId] = subscription.artProfileId
                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            }

            SubscriptionTable
                .selectAll()
                .where { SubscriptionTable.id eq insertResult[SubscriptionTable.id] }
                .single()
                .let { toSubscription(it) }
        }
    }

    fun existsByMemberIdAndArtProfileId(memberId: Long, artProfileId: Long): Boolean {
        return transaction {
            SubscriptionTable
                .selectAll()
                .where { (SubscriptionTable.memberId eq memberId) and (SubscriptionTable.artProfileId eq artProfileId) }
                .any()
        }
    }

    fun deleteByMemberIdAndArtProfileId(memberId: Long, artProfileId: Long): Boolean {
        return transaction {
            val deletedCount = SubscriptionTable.deleteWhere {
                (SubscriptionTable.memberId eq memberId) and (SubscriptionTable.artProfileId eq artProfileId)
            }
            deletedCount > 0
        }
    }

    fun findByMemberId(memberId: Long): List<Subscription> {
        return transaction {
            SubscriptionTable
                .selectAll()
                .where { SubscriptionTable.memberId eq memberId }
                .map { toSubscription(it) }
        }
    }

    private fun toSubscription(row: ResultRow): Subscription {
        val offsetDateTime = row[SubscriptionTable.createdAt]
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
        
        return Subscription(
            id = row[SubscriptionTable.id],
            memberId = row[SubscriptionTable.memberId],
            artProfileId = row[SubscriptionTable.artProfileId],
            createdAt = localDateTime
        )
    }
}

