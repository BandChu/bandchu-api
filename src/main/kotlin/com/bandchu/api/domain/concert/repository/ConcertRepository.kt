package com.bandchu.api.domain.concert.repository

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.service.dto.CreateConcertCommand
import com.bandchu.api.domain.concert.service.dto.UpdateConcertCommand
import com.bandchu.api.domain.concert.table.ConcertScheduleTable
import com.bandchu.api.domain.concert.table.ConcertTable
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.core.ResultRow
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

    private fun ResultRow.toDomain(): Concert =
        Concert(
            id = this[ConcertTable.id].value,
            title = this[ConcertTable.title],
            place = this[ConcertTable.place],
            posterImageUrl = this[ConcertTable.posterImageUrl]?.let { URI(it) },
            information = this[ConcertTable.information],
            bookingUrl = this[ConcertTable.bookingUrl]?.let { URI(it) },
            bookingSchedule = this[ConcertTable.bookingSchedule],
            createdAt = this[ConcertTable.createdAt],
            artiProfileId = this[ConcertTable.arti_profile].value
        )

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

        ConcertTable
            .selectAll()
            .where { ConcertTable.id eq concertId }
            .single()
            .toDomain()
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

        ConcertTable
            .selectAll()
            .where { ConcertTable.id eq command.concertId }
            .first()
            .toDomain()
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

        row.toDomain()
    }
}