package com.bandchu.api.domain.concert.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ConcertScheduleTable : LongIdTable("concert_schedule") {
    val date = timestampWithTimeZone("date")

    val concert = reference("concert", ConcertTable.id, onDelete = ReferenceOption.CASCADE)
}