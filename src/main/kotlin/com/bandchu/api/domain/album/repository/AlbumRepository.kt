package com.bandchu.api.domain.album.repository

import com.bandchu.api.domain.album.model.Album
import com.bandchu.api.domain.album.model.Track
import com.bandchu.api.domain.album.service.dto.CreateAlbumCommand
import com.bandchu.api.domain.album.table.AlbumTable
import com.bandchu.api.domain.album.table.TrackTable
import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.net.URI

@Repository
class AlbumRepository {
    private fun ResultRow.toDomain(tracks: List<Track>): Album =
        Album(
            id = this[AlbumTable.id].value,
            name = this[AlbumTable.name],
            releaseDate = this[AlbumTable.releaseDate],
            coverImageUrl = this[AlbumTable.coverImageUrl]?.let { URI(it) },
            description = this[AlbumTable.description],
            artiProfileId = this[AlbumTable.artiProfile].value,
            tracks = tracks
            )

    private fun ResultRow.toTrackDomain(): Track =
        Track(
            id = this[TrackTable.id].value,
            name = this[TrackTable.name],
            url = URI(this[TrackTable.url]),
            albumId = this[TrackTable.album].value
        )

    private fun findTracksByAlbumId(albumId: Long): List<Track> {
        return TrackTable
            .selectAll()
            .where { TrackTable.album eq albumId }
            .orderBy(TrackTable.id, SortOrder.ASC)
            .map { it.toTrackDomain() }
            .toList()
    }

    fun getDetail(albumId: Long): Album = transaction {
        val albumRow = AlbumTable
            .selectAll()
            .where { AlbumTable.id eq  albumId }
            .singleOrNull()
            ?: throw BusinessException(ErrorCode.ALBUM_NOT_FOUND)

        val tracks = findTracksByAlbumId(albumId)

        albumRow.toDomain(tracks)
    }

    fun createProcess(command: CreateAlbumCommand, userId: Long): Album = transaction {
        val artiProfileId = ArtiProfileTable
            .select(ArtiProfileTable.id)
            .where { ArtiProfileTable.member eq userId }
            .limit(1)
            .map { it[ArtiProfileTable.id].value }
            .firstOrNull()
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_CREATED)

        val albumId = AlbumTable.insertAndGetId {
            it[name] = command.name
            it[releaseDate] = command.releaseDate
            it[coverImageUrl] = command.coverImageUrl?.toString()
            it[description] = command.description
            it[artiProfile] = artiProfileId
        }

        if (command.tracks.isNotEmpty()) {
            TrackTable.batchInsert(command.tracks) { trackCommand ->
                this[TrackTable.name] = trackCommand.name
                this[TrackTable.url] = trackCommand.url.toString()
                this[TrackTable.album] = albumId
            }
        }

        val albumRow = AlbumTable
            .selectAll()
            .where { AlbumTable.id eq  albumId }
            .single()

        val tracks = findTracksByAlbumId(albumId.value)

        albumRow.toDomain(tracks)
    }

    fun delete(albumId: Long, userId: Long): Unit = transaction {
        val artiProfileId = ArtiProfileTable
            .select(ArtiProfileTable.id)
            .where { ArtiProfileTable.member eq userId }
            .single()[ArtiProfileTable.id]
            .value

        val albumRow = AlbumTable
            .select(AlbumTable.artiProfile)
            .where { AlbumTable.id eq albumId }
            .limit(1)
            .singleOrNull()
            ?: throw BusinessException(ErrorCode.ALBUM_NOT_FOUND)

        val currentOwnerProfileId = albumRow[AlbumTable.artiProfile].value

        if (currentOwnerProfileId != artiProfileId) {
            throw BusinessException(ErrorCode.ARTIST_FORBIDDEN)
        }

        AlbumTable.deleteWhere { AlbumTable.id eq albumId }
    }

    fun getAllSummaryByArtist(artistId: Long): List<Album> = transaction {
        val albumRows = AlbumTable
            .selectAll()
            .where { AlbumTable.artiProfile eq artistId }
            .toList()

        albumRows.map { albumRow ->
            albumRow.toDomain(emptyList())
        }
    }
}