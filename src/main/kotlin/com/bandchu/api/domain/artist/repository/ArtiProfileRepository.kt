package com.bandchu.api.domain.artist.repository

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.table.ConcertTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.net.URI

@Repository
class ArtiProfileRepository {

    private fun ResultRow.toDomain(): ArtiProfile = ArtiProfile(
        id = this[ArtiProfileTable.id].value,
        artistName = this[ArtiProfileTable.artistName],
        genre = this[ArtiProfileTable.genre],
        description = this[ArtiProfileTable.description],
        profileImageUrl = this[ArtiProfileTable.profileImageUrl]?.let { URI(it) },
        createdAt = this[ArtiProfileTable.createdAt],
        updatedAt = this[ArtiProfileTable.updatedAt],
        memberId = this[ArtiProfileTable.member]
    )

    private fun ResultRow.toConcertDomain(): Concert =
        Concert(
            id = this[ConcertTable.id].value,
            title = this[ConcertTable.title],
            place = this[ConcertTable.place],
            posterImageUrl = this[ConcertTable.posterImageUrl]?.let { URI(it) },
            information = this[ConcertTable.information],
            bookingUrl = this[ConcertTable.bookingUrl]?.let { URI(it) },
            bookingSchedule = this[ConcertTable.bookingSchedule],
            createdAt = this[ConcertTable.createdAt]
        )

    fun findAll(): List<ArtiProfile> = transaction {
        ArtiProfileTable
            .selectAll()
            .map { it.toDomain() }
    }

    fun searchArtistsAndConcerts(keyword: String): Pair<List<ArtiProfile>, List<Concert>> = transaction {
        val pattern = "%$keyword%"

        val artists = ArtiProfileTable
            .selectAll()
            .where { ArtiProfileTable.artistName like pattern }
            .map { it.toDomain() }

        val concerts = ConcertTable
            .join(
                ArtiProfileTable,
                JoinType.LEFT,
                onColumn = ConcertTable.arti_profile,
                otherColumn = ArtiProfileTable.id
            )
            .select(ConcertTable.columns)
            .where { (ArtiProfileTable.artistName like pattern) or
                    (ConcertTable.title like pattern) }
            .withDistinct()
            .map { it.toConcertDomain() }

        artists to concerts
    }
}