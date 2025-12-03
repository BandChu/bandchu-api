package com.bandchu.api.domain.artist.repository

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.model.Genre
import com.bandchu.api.domain.artist.model.SnsLink
import com.bandchu.api.domain.artist.service.dto.CreateArtistDetailCommand
import com.bandchu.api.domain.artist.service.dto.UpdateArtistDetailCommand
import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.artist.table.SnsLinkTable
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.table.ConcertTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
        memberId = this[ArtiProfileTable.member]?.value,
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
            createdAt = this[ConcertTable.createdAt],
            artiProfileId = this[ConcertTable.arti_profile].value
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

    fun findByIds(ids: List<Long>): List<ArtiProfile> = transaction {
        if (ids.isEmpty()) {
            return@transaction emptyList()
        }

        val artistRows = ArtiProfileTable
            .selectAll()
            .where { ArtiProfileTable.id inList ids }
            .map { it }

        if (artistRows.isEmpty()) {
            return@transaction emptyList()
        }

        val artiProfileIds = artistRows.map { it[ArtiProfileTable.id].value }
        val snsLinksMap = SnsLinkTable
            .selectAll()
            .where { SnsLinkTable.artiProfile inList artiProfileIds }
            .groupBy { it[SnsLinkTable.artiProfile].value }
            .mapValues { (_, rows) -> rows.map { it.toSnsLinkDomain() } }

        artistRows.map { row ->
            val artiProfileId = row[ArtiProfileTable.id].value
            row.toDomain(snsLinks = snsLinksMap[artiProfileId] ?: emptyList())
        }
    }

    fun createProcess(command: CreateArtistDetailCommand, memberId: Long): ArtiProfile? = transaction {
        val artiProfileId = ArtiProfileTable.insertAndGetId {
            it[artistName] = command.name
            it[profileImageUrl] = command.profileImageUrl?.toString()
            it[description] = command.description
            it[genre] = command.genre.map { it.name }
            it[member] = memberId
            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }

        if (!command.sns.isEmpty()) {
            val insertedRows = SnsLinkTable.batchInsert(command.sns) { cmd ->
                this[SnsLinkTable.artiProfile] = artiProfileId
                this[SnsLinkTable.platform] = cmd.platform
                this[SnsLinkTable.url] = cmd.url
            }
        }

        val artistRow = ArtiProfileTable
            .selectAll()
            .where { ArtiProfileTable.id eq artiProfileId }
            .singleOrNull()

        if (artistRow == null) {
            return@transaction null
        }

        val snsLinks = SnsLinkTable
            .selectAll()
            .where { SnsLinkTable.artiProfile eq artiProfileId }
            .map { it.toSnsLinkDomain() }

        artistRow.toDomain(snsLinks = snsLinks)
    }

    fun updateProcess(command: UpdateArtistDetailCommand): ArtiProfile? = transaction {
        ArtiProfileTable.update({ ArtiProfileTable.id eq command.artistId}) {
            it[artistName] = command.name
            it[profileImageUrl] = command.profileImageUrl?.toString()
            it[description] = command.description
            it[genre] = command.genre.map { it.name }
            it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }

        SnsLinkTable.deleteWhere { SnsLinkTable.artiProfile eq command.artistId }

        if (!command.sns.isEmpty()) {
            SnsLinkTable.batchInsert(command.sns) { cmd ->
                this[SnsLinkTable.artiProfile] = command.artistId
                this[SnsLinkTable.platform] = cmd.platform
                this[SnsLinkTable.url] = cmd.url
            }
        }

        val artistRow = ArtiProfileTable
            .selectAll()
            .where { ArtiProfileTable.id eq command.artistId }
            .singleOrNull()

        if (artistRow == null) {
            return@transaction null
        }

        val snsLinks = SnsLinkTable
            .selectAll()
            .where { SnsLinkTable.artiProfile eq command.artistId }
            .map { it.toSnsLinkDomain() }

        artistRow.toDomain(snsLinks = snsLinks)
    }

    fun findByMemberId(memberId: Long): ArtiProfile? = transaction {
        ArtiProfileTable
            .selectAll()
            .where { ArtiProfileTable.member eq memberId }
            .singleOrNull()
            ?.toDomain()
    }
}