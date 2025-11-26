package com.bandchu.api.domain.artist.repository

import com.bandchu.api.domain.artist.model.SnsLink
import com.bandchu.api.domain.artist.service.dto.UpdateArtistSnsCommand
import com.bandchu.api.domain.artist.table.SnsLinkTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.springframework.stereotype.Repository
import java.net.URI

@Repository
class SnsLinkRepository {

    private fun ResultRow.toDomain(): SnsLink {
        return SnsLink(
            id = this[SnsLinkTable.id].value,
            platform = this[SnsLinkTable.platform],
            url = URI(this[SnsLinkTable.url])
        )
    }

    fun replaceAll(artiProfileId: Long, snsCommands: List<UpdateArtistSnsCommand>) {
        SnsLinkTable.deleteWhere { SnsLinkTable.artiProfile eq artiProfileId }

        if (snsCommands.isEmpty()) return

        SnsLinkTable.batchInsert(snsCommands) { command ->
            this[SnsLinkTable.artiProfile] = artiProfileId
            this[SnsLinkTable.platform] = command.platform
            this[SnsLinkTable.url] = command.url
        }
    }
}