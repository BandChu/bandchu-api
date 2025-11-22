package com.bandchu.api.domain.posts.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ReportTable : Table("reports") {

val id = integer("report_id").autoIncrement()

    val reported_at = datetime("reported_at")

    val reason = varchar("reason", 20)

    val post_id = long("post_id").references(PostTable.id)
    override val primaryKey = PrimaryKey(id)


}