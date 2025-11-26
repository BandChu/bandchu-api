package com.bandchu.api.domain.concert.table

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ConcertTable : LongIdTable("concerts") {
    val title = varchar("title", 30)
    val place = varchar("place", 20)
    val posterImageUrl = varchar("poster_image_url", 255).nullable()
    val information = text("information").nullable()
    val bookingUrl = varchar("booking_url", 255).nullable()
    val bookingSchedule = timestampWithTimeZone("booking_date").nullable()
    val createdAt = timestampWithTimeZone("created_at")

    val arti_profile = reference("arti_profile", ArtiProfileTable.id, onDelete = ReferenceOption.CASCADE)
}