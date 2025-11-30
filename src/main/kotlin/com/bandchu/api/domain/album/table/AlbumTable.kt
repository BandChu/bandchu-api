package com.bandchu.api.domain.album.table

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object AlbumTable : LongIdTable("albums") {
    val name = varchar("name", 30)
    val releaseDate = timestampWithTimeZone("release_date")
    val coverImageUrl = varchar("cover_image_url", 255).nullable()
    val description = text("description").nullable()

    val artiProfile = reference("arti_profile", ArtiProfileTable.id, ReferenceOption.CASCADE)
}