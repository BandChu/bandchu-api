package com.bandchu.api.domain.subscription.table

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object SubscriptionTable : Table("subscriptions") {
    val id = long("id").autoIncrement()
    val member = reference("member", MemberTable.id, onDelete = ReferenceOption.CASCADE)
    val artiProfile = reference("arti_profile", ArtiProfileTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(member, artiProfile)
    }
}

