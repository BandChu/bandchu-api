package com.bandchu.api.domain.artist.repository

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.model.Genre
import com.bandchu.api.domain.artist.model.SnsLink
import com.bandchu.api.domain.artist.service.dto.UpdateArtistDetailCommand
import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.artist.table.SnsLinkTable
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.table.ConcertTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.net.URI

@Repository
class ArtiProfileRepository {

    private fun ResultRow.toDomain(
        snsLinks: List<SnsLink> = emptyList()
    ): ArtiProfile = ArtiProfile(
        id = this[ArtiProfileTable.id].value,
        artistName = this[ArtiProfileTable.artistName],
        genre = this[ArtiProfileTable.genre].map { Genre.valueOf(it) },
        description = this[ArtiProfileTable.description],
        profileImageUrl = this[ArtiProfileTable.profileImageUrl]?.let { URI(it) },
        createdAt = this[ArtiProfileTable.createdAt],
        updatedAt = this[ArtiProfileTable.updatedAt],
        memberId = this[ArtiProfileTable.member],
        snsLinks = snsLinks
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

    private fun ResultRow.toSnsLinkDomain(): SnsLink =
        SnsLink(
            id = this[SnsLinkTable.id].value,
            platform = this[SnsLinkTable.platform],
            url = URI(this[SnsLinkTable.url])
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

    fun findById(id: Long): ArtiProfile? = transaction {
        val artistRow = ArtiProfileTable
            .selectAll()
            .where { ArtiProfileTable.id eq id }
            .singleOrNull()

        if (artistRow == null) {
            return@transaction null
        }

        val snsLinks = SnsLinkTable
            .selectAll()
            .where { SnsLinkTable.artiProfile eq id }
            .map { it.toSnsLinkDomain() }

        artistRow.toDomain(snsLinks = snsLinks)
    }

    fun updateArtist(command: UpdateArtistDetailCommand) {
        ArtiProfileTable.update({ ArtiProfileTable.id eq command.artistId}) {
            it[artistName] = command.name
            it[profileImageUrl] = command.profileImageUrl
            it[description] = command.description
            it[genre] = command.genre.map { it.name }
        }
    }
}