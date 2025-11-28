package com.bandchu.api.global.config


import com.bandchu.api.domain.chat.table.ChatMessageTable
import com.bandchu.api.domain.chat.table.ChatRoomTable
import com.bandchu.api.domain.chat.table.MemberChatRoomTable
import com.bandchu.api.domain.chat.table.MessageType
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.domain.member.table.MemberTable
import com.bandchu.api.domain.posts.table.CommentTable
import com.bandchu.api.domain.posts.table.MediaTable
import com.bandchu.api.domain.posts.table.PostTable
import com.bandchu.api.domain.posts.table.ReportTable
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.datetime.Minute
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import java.time.OffsetDateTime

import javax.sql.DataSource
@Configuration
class DatabaseConfig(private val dataSource: DataSource) {

    @EventListener(ContextRefreshedEvent::class)
    fun init() {
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(PostTable, CommentTable, MediaTable, ReportTable)
            SchemaUtils.create(ChatMessageTable, ChatRoomTable, MemberChatRoomTable)
        }

        println(" Exposed + Spring Boot 연결 성공")

    }
}
