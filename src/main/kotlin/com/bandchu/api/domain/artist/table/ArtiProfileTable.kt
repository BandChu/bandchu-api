package com.bandchu.api.domain.artist.table

import com.bandchu.api.domain.posts.table.PostTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ArtiProfileTable : Table("artist_profile") {

val id = long("artist_id").autoIncrement()
override val primaryKey = PrimaryKey(id)
}