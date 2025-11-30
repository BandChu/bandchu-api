package com.bandchu.api.domain.concert.repository

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.model.Genre
import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.model.ConcertSchedule
import com.bandchu.api.domain.concert.service.dto.ConcertSubscribedRead
import com.bandchu.api.domain.concert.service.dto.CreateConcertCommand
import com.bandchu.api.domain.concert.service.dto.UpdateConcertCommand
import com.bandchu.api.domain.concert.table.ConcertScheduleTable
import com.bandchu.api.domain.concert.table.ConcertTable
import com.bandchu.api.domain.subscription.table.SubscriptionTable
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class ConcertRepository {

    private fun ResultRow.toDomain(schedules: List<ConcertSchedule>): Concert =
        Concert(
            id = this[ConcertTable.id].value,
            title = this[ConcertTable.title],
            place = this[ConcertTable.place],
            posterImageUrl = this[ConcertTable.posterImageUrl]?.let { URI(it) },
            information = this[ConcertTable.information],
            bookingUrl = this[ConcertTable.bookingUrl]?.let { URI(it) },
            bookingSchedule = this[ConcertTable.bookingSchedule],
            createdAt = this[ConcertTable.createdAt],
            artiProfileId = this[ConcertTable.arti_profile].value,
            schedules = schedules
        )

    private fun ResultRow.toConcertScheduleDomain(concertId: Long): ConcertSchedule {
        return ConcertSchedule(
            id = this[ConcertScheduleTable.id].value,
            date = this[ConcertScheduleTable.date],
            concertId = concertId
        )
    }

    private fun ResultRow.toArtiProfileDomain(): ArtiProfile =
        ArtiProfile(
            id = this[ArtiProfileTable.id].value,
            artistName = this[ArtiProfileTable.artistName],
            genre = this[ArtiProfileTable.genre].map { Genre.valueOf(it) },
            description = this[ArtiProfileTable.description],
            profileImageUrl = this[ArtiProfileTable.profileImageUrl]?.let { URI(it) },
            createdAt = this[ArtiProfileTable.createdAt],
            updatedAt = this[ArtiProfileTable.updatedAt],
            memberId = this[ArtiProfileTable.member].value
        )

    private fun findSchedulesByConcertId(concertId: Long): List<ConcertSchedule> = transaction {
        ConcertScheduleTable
            .selectAll()
            .where { ConcertScheduleTable.concert eq concertId }
            .orderBy(ConcertScheduleTable.date, SortOrder.ASC)
            .map { row ->
                row.toConcertScheduleDomain(concertId)
            }
            .toList()
    }

    fun createProcess(command: CreateConcertCommand, userId: Long): Concert = transaction {
        val artiProfileId = ArtiProfileTable
            .select(ArtiProfileTable.id)
            .where { ArtiProfileTable.member eq userId }
            .limit(1)
            .map { it[ArtiProfileTable.id].value }
            .firstOrNull()
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_CREATED)

        val concertId = ConcertTable.insertAndGetId {
            it[title] = command.title
            it[place] = command.place
            it[posterImageUrl] = command.posterImageUrl?.toString()
            it[information] = command.information
            it[bookingUrl] = command.bookingUrl?.toString()
            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            it[arti_profile] = artiProfileId
        }

        if (command.performingSchedule.isNotEmpty()) {
            ConcertScheduleTable.batchInsert(command.performingSchedule) { scheduleCommand ->
                this[ConcertScheduleTable.date] = scheduleCommand.date
                this[ConcertScheduleTable.concert] = concertId
            }
        }

        val schedules = findSchedulesByConcertId(concertId.value)

        ConcertTable
            .selectAll()
            .where { ConcertTable.id eq concertId }
            .single()
            .toDomain(schedules)
    }

    fun updateProcess(command: UpdateConcertCommand, userId: Long): Concert = transaction {
        val artiProfileId = ArtiProfileTable
            .select(ArtiProfileTable.id)
            .where { ArtiProfileTable.member eq userId }
            .limit(1)
            .single()[ArtiProfileTable.id].value

        val concertRow = ConcertTable
            .select(ConcertTable.arti_profile)
            .where { ConcertTable.id eq command.concertId }
            .limit(1)
            .singleOrNull()

        if (concertRow == null) throw BusinessException(ErrorCode.CONCERT_NOT_FOUND)

        val currentOwnerProfileId = concertRow[ConcertTable.arti_profile].value

        if (currentOwnerProfileId != artiProfileId) throw BusinessException(ErrorCode.ARTIST_FORBIDDEN)

        ConcertTable.update({ ConcertTable.id eq command.concertId }) {
            it[title] = command.title
            it[place] = command.place
            it[posterImageUrl] = command.posterImageUrl?.toString()
            it[information] = command.information
            it[bookingUrl] = command.bookingUrl?.toString()
        }

        if (command.performingSchedule.isNotEmpty()) {
            ConcertScheduleTable.deleteWhere { ConcertScheduleTable.concert eq command.concertId }

            ConcertScheduleTable.batchInsert(command.performingSchedule) { scheduleCommand ->
                this[ConcertScheduleTable.date] = scheduleCommand.date
                this[ConcertScheduleTable.concert] = command.concertId
            }
        }

        val schedules = findSchedulesByConcertId(command.concertId)

        ConcertTable
            .selectAll()
            .where { ConcertTable.id eq command.concertId }
            .first()
            .toDomain(schedules)
    }

    fun delete(concertId: Long, userId: Long): Unit = transaction {
        val artiProfileId = ArtiProfileTable
            .select(ArtiProfileTable.id)
            .where { ArtiProfileTable.member eq userId }
            .single()[ArtiProfileTable.id]
            .value

        val concertRow = ConcertTable
            .select(ConcertTable.arti_profile)
            .where { ConcertTable.id eq concertId }
            .singleOrNull()

        if (concertRow == null) throw BusinessException(ErrorCode.CONCERT_NOT_FOUND)

        val ownerProfileId = concertRow[ConcertTable.arti_profile].value

        if (ownerProfileId != artiProfileId) throw BusinessException(ErrorCode.ARTIST_FORBIDDEN)

        ConcertTable.deleteWhere { ConcertTable.id eq concertId }
    }

    fun getDetail(concertId: Long): Concert = transaction {
        val row = ConcertTable
            .selectAll()
            .where { ConcertTable.id eq concertId }
            .limit(1)
            .singleOrNull()
            ?: throw BusinessException(ErrorCode.CONCERT_NOT_FOUND)

        val schedules = findSchedulesByConcertId(concertId)

        row.toDomain(schedules)
    }

    fun getConcertsBySubscription(userId: Long): List<ConcertSubscribedRead> = transaction {
        val rawRows = SubscriptionTable
            .join(
                ArtiProfileTable,
                JoinType.INNER,
                onColumn = SubscriptionTable.artiProfile,
                otherColumn = ArtiProfileTable.id
            )
            .join(
                ConcertTable,
                JoinType.INNER,
                onColumn = ArtiProfileTable.id,
                otherColumn = ConcertTable.arti_profile
            )
            .join(
                ConcertScheduleTable,
                JoinType.LEFT,
                onColumn = ConcertTable.id,
                otherColumn = ConcertScheduleTable.concert
            )
            .selectAll()
            .where { SubscriptionTable.member eq userId }
            .orderBy(SubscriptionTable.createdAt, SortOrder.DESC)
            .toList()

        if (rawRows.isEmpty()) return@transaction emptyList()

        val groupedByArtist = rawRows.groupBy { it[ArtiProfileTable.id] }

        return@transaction groupedByArtist.map { (_, rows) ->

            val profile = rows.first().toArtiProfileDomain()
            val subscribedAt = rows.first()[SubscriptionTable.createdAt]
            val concertId = rows.first()[ConcertTable.id].value
            val groupedByConcert = rows.groupBy { it[ConcertTable.id] }
            val concerts = groupedByConcert.map { (_, concertRows) ->

                val schedules = concertRows
                    .mapNotNull { row ->
                        if (row.hasValue(ConcertScheduleTable.date)) row.toConcertScheduleDomain(concertId) else null
                    }
                    .distinct()

                concertRows.first().toDomain(schedules)
            }

            ConcertSubscribedRead(
                artists = profile,
                subscribedAt = subscribedAt,
                concerts = concerts
            )
        }
    }
}