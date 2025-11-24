package com.bandchu.api.domain.posts.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object MediaTable : Table("medias") {

val id = integer("media_id").autoIncrement()

    val created_at = datetime("created_at")

    val updated_at = datetime("updated_at")

    val post_id = long("post_id").references(PostTable.id)

    override val primaryKey = PrimaryKey(id)
}