package com.bandchu.api.domain.member.table

import com.bandchu.api.domain.posts.table.PostTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object MemberTable : Table("members") {

val id = long("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
}
