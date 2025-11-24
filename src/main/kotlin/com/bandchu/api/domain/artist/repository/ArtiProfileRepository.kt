package com.bandchu.api.domain.artist.repository

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.table.ArtiProfileTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ArtiProfileRepository {

    private fun ResultRow.toDomain(): ArtiProfile = ArtiProfile(
        id = this[ArtiProfileTable.id].value,
        artistName = this[ArtiProfileTable.artistName],
        genre = this[ArtiProfileTable.genre],
        description = this[ArtiProfileTable.description],
        profileImageUrl = this[ArtiProfileTable.profileImageUrl],
        createdAt = this[ArtiProfileTable.createdAt],
        updatedAt = this[ArtiProfileTable.updatedAt],
        memberId = this[ArtiProfileTable.member]
    )
    fun findAll(): List<ArtiProfile> = transaction {
        ArtiProfileTable
            .selectAll()
            .map { it.toDomain() }
    }
}