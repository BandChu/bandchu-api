package com.bandchu.api.global.config


import com.bandchu.api.domain.album.table.AlbumTable
import com.bandchu.api.domain.album.table.TrackTable
import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.artist.table.SnsLinkTable
import com.bandchu.api.domain.chat.table.ChatMessageTable
import com.bandchu.api.domain.chat.table.ChatRoomTable
import com.bandchu.api.domain.chat.table.MemberChatRoomTable
import com.bandchu.api.domain.concert.table.ConcertScheduleTable
import com.bandchu.api.domain.concert.table.ConcertTable
import com.bandchu.api.domain.friend.table.FriendTable
import com.bandchu.api.domain.member.table.MemberTable
import com.bandchu.api.domain.posts.table.CommentTable
import com.bandchu.api.domain.posts.table.MediaTable
import com.bandchu.api.domain.posts.table.PostTable
import com.bandchu.api.domain.posts.table.ReportTable
import com.bandchu.api.domain.subscription.table.SubscriptionTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import javax.sql.DataSource

@Configuration
class DatabaseConfig(
    private val dataSource: DataSource
) {

    @EventListener(ContextRefreshedEvent::class)
    fun init() {
        Database.connect(dataSource)

        // 전체 테이블 생성
        transaction {
            exec("CREATE EXTENSION IF NOT EXISTS cube")
            exec("CREATE EXTENSION IF NOT EXISTS earthdistance")

            SchemaUtils.create(
                // member & subscription
                MemberTable,
                SubscriptionTable,

                // posts
                PostTable,
                CommentTable,
                MediaTable,
                ReportTable,

                // chat
                ChatRoomTable,
                ChatMessageTable,
                MemberChatRoomTable,

                // artist
                ArtiProfileTable,
                SnsLinkTable,

                // concert
                ConcertTable,
                ConcertScheduleTable,
                AlbumTable,
                TrackTable,

                // friends
                FriendTable,
            )
        }

        println("Exposed + Spring Boot 연결 성공")
    }
}
