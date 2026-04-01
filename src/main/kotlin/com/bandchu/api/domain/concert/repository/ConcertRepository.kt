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
import org.jetbrains.exposed.v1.core.DoubleColumnType
import org.jetbrains.exposed.v1.datetime.KotlinOffsetDateTimeColumnType
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Repository
class ConcertRepository {

    private fun ResultRow.toDomain(schedules: List<ConcertSchedule>): Concert =
        Concert(
            id = this[ConcertTable.id].value,
            title = this[ConcertTable.title],
            place = this[ConcertTable.place],
            latitude = this[ConcertTable.latitude],
            longitude = this[ConcertTable.longitude],
            posterImageUrl = this[ConcertTable.posterImageUrl]?.let { URI(it) },
            information = this[ConcertTable.information],
            bookingUrl = this[ConcertTable.bookingUrl]?.let { URI(it) },
            bookingSchedule = this[ConcertTable.bookingSchedule],
            createdAt = this[ConcertTable.createdAt],
            artiProfileId = this[ConcertTable.arti_profile].value,
            viewCount = this[ConcertTable.viewCount],
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
            memberId = this[ArtiProfileTable.member]?.value
        )

    private fun findSchedulesByConcertId(concertId: Long): List<ConcertSchedule> =
        ConcertScheduleTable
            .selectAll()
            .where { ConcertScheduleTable.concert eq concertId }
            .orderBy(ConcertScheduleTable.date, SortOrder.ASC)
            .map { row ->
                row.toConcertScheduleDomain(concertId)
            }
            .toList()

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
            it[latitude] = command.latitude
            it[longitude] = command.longitude
            it[posterImageUrl] = command.posterImageUrl?.toString()
            it[information] = command.information
            it[bookingSchedule] = command.bookingSchedule
            it[bookingUrl] = command.bookingUrl?.toString()
            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            it[viewCount] = 0L
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
            it[latitude] = command.latitude
            it[longitude] = command.longitude
            it[information] = command.information
            it[bookingSchedule] = command.bookingSchedule
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

    fun incrementViewCount(concertId: Long): Int = transaction {
        ConcertTable.update({ ConcertTable.id eq concertId }) {
            it[viewCount] = ConcertTable.viewCount + 1L
        }
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
                JoinType.LEFT,
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

            val firstRow = rows.first()
            val profile = firstRow.toArtiProfileDomain()
            val subscribedAt = firstRow[SubscriptionTable.createdAt]

            val groupedByConcert = rows.groupBy { it.getOrNull(ConcertTable.id) }

            val concerts = groupedByConcert.mapNotNull { (concertIdEntity, concertRows) ->

                if (concertIdEntity == null) {
                    return@mapNotNull null
                }

                val concertId = concertIdEntity.value

                val schedules = concertRows
                    .mapNotNull { row ->
                        if (row.hasValue(ConcertScheduleTable.date)) {
                            row.toConcertScheduleDomain(concertId)
                        } else {
                            null
                        }
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

    fun findNearbyConcerts(lat: Double, lng: Double): List<Concert> = transaction {
        val kst = ZoneId.of("Asia/Seoul")
        val today = LocalDate.now(kst)
        val todayStart = today.atStartOfDay(kst).toOffsetDateTime()
        val tomorrowStart = today.plusDays(1).atStartOfDay(kst).toOffsetDateTime()

        val sql = """
            SELECT c.id, c.title, c.place, c.latitude, c.longitude,
                   c.poster_image_url, c.information, c.booking_url, c.booking_date,
                   c.created_at, c.view_count, c.arti_profile
            FROM concerts c
            JOIN (
                SELECT concert, MIN(date) AS earliest_today
                FROM concert_schedule
                WHERE date >= ?
                  AND date < ?
                GROUP BY concert
            ) cs_today ON cs_today.concert = c.id
            WHERE c.latitude IS NOT NULL
              AND c.longitude IS NOT NULL
              AND earth_box(ll_to_earth(?, ?), 10000) @> ll_to_earth(c.latitude::float8, c.longitude::float8)
              AND earth_distance(ll_to_earth(c.latitude::float8, c.longitude::float8), ll_to_earth(?, ?)) <= 10000
            ORDER BY cs_today.earliest_today ASC, c.view_count DESC
        """.trimIndent()

        val doubleType = DoubleColumnType()
        val tsType = KotlinOffsetDateTimeColumnType()
        val concerts = exec(
            sql,
            args = listOf(
                tsType to todayStart,
                tsType to tomorrowStart,
                doubleType to lat,
                doubleType to lng,
                doubleType to lat,
                doubleType to lng,
            )
        ) { rs ->
            val result = mutableListOf<Concert>()
            while (rs.next()) {
                result.add(
                    Concert(
                        id = rs.getLong("id"),
                        title = rs.getString("title"),
                        place = rs.getString("place"),
                        latitude = rs.getBigDecimal("latitude"),
                        longitude = rs.getBigDecimal("longitude"),
                        posterImageUrl = rs.getString("poster_image_url")?.let { URI(it) },
                        information = rs.getString("information"),
                        bookingUrl = rs.getString("booking_url")?.let { URI(it) },
                        bookingSchedule = rs.getObject("booking_date", OffsetDateTime::class.java),
                        createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                        artiProfileId = rs.getLong("arti_profile"),
                        viewCount = rs.getLong("view_count"),
                        schedules = emptyList()
                    )
                )
            }
            result
        } ?: emptyList()

        if (concerts.isEmpty()) return@transaction emptyList()

        val concertIds = concerts.map { it.id }
        val todaySchedules = ConcertScheduleTable
            .selectAll()
            .where {
                ConcertScheduleTable.concert.inList(concertIds)
                    .and(ConcertScheduleTable.date greaterEq todayStart)
                    .and(ConcertScheduleTable.date less tomorrowStart)
            }
            .map { row -> row.toConcertScheduleDomain(row[ConcertScheduleTable.concert].value) }
            .groupBy { it.concertId }

        concerts.map { concert ->
            concert.copy(schedules = todaySchedules[concert.id] ?: emptyList())
        }
    }

    fun getAllByArtist(artistId: Long): List<Concert> = transaction {
        val concertRows = ConcertTable
            .selectAll()
            .where { ConcertTable.arti_profile eq artistId }
            .orderBy(ConcertTable.createdAt, SortOrder.DESC)
            .toList()

        val concertIds = concertRows.map { it[ConcertTable.id].value }

        val allSchedules = ConcertScheduleTable
            .selectAll()
            .where { ConcertScheduleTable.concert inList concertIds }
            .map { row -> row.toConcertScheduleDomain(row[ConcertScheduleTable.concert].value) }
            .groupBy { it.concertId }

        return@transaction concertRows.map { concertRow ->
            val concertId = concertRow[ConcertTable.id].value
            val schedules = allSchedules[concertId] ?: emptyList()

            concertRow.toDomain(schedules)
        }
    }
}