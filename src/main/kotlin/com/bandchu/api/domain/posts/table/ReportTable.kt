package com.bandchu.api.domain.posts.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ReportTable : Table("report") {
    val id = long("report_id").autoIncrement()

    val reportAt = timestampWithTimeZone("report_at")
    val reason = varchar("reason", length = 20)

    val postId = long("post_id")
        .references(PostTable.id)

    override val primaryKey = PrimaryKey(id)
}
