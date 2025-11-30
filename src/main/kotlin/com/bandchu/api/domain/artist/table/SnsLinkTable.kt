package com.bandchu.api.domain.artist.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object SnsLinkTable : LongIdTable("sns_links") {
    val platform = varchar("platform", 10)
    val url = varchar("url", 255)

    val artiProfile = reference("arti_profile", ArtiProfileTable.id, onDelete = ReferenceOption.CASCADE)
}