package com.bandchu.api.domain.subscription.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object SubscriptionTable : Table("subscriptions") {
    val id = long("id").autoIncrement()
    val memberId = long("member_id")
    val artiProfileId = long("arti_profile_id")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(memberId, artiProfileId)
    }
}

