package com.bandchu.api.fixture

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.subscription.service.SubscriptionService
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class SubscriptionFixture(
    private val subscriptionService: SubscriptionService,
    private val authFixture: AuthFixture
) {

    /**
     * 테스트용 ArtiProfile 생성
     */
    fun createArtiProfile(
        artistName: String,
        artist: Member? = null,
        genre: List<String> = listOf("POP"),
        description: String? = "테스트 아티스트입니다"
    ): Long {
        return transaction {
            ArtiProfileTable.insertAndGetId {
                it[ArtiProfileTable.artistName] = artistName
                it[ArtiProfileTable.genre] = genre
                it[ArtiProfileTable.description] = description
                it[ArtiProfileTable.profileImageUrl] = "https://example.com/profile.jpg"
                it[ArtiProfileTable.createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[ArtiProfileTable.updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[ArtiProfileTable.member] = artist?.id
            }.value
        }
    }

    /**
     * 구독 생성 (Service 호출)
     */
    fun subscribe(fan: Member, artiProfileId: Long) {
        authFixture.authenticateAs(fan)
        subscriptionService.subscribe(fan.id!!, artiProfileId)
    }
}
