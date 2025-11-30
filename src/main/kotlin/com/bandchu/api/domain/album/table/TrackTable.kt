package com.bandchu.api.domain.album.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object TrackTable : LongIdTable("tracks") {
    val name = varchar("name", 30)
    val url = varchar("url", 255)

    val album = reference("album", AlbumTable.id, ReferenceOption.CASCADE)
}