package com.bandchu.api.global.config

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import com.bandchu.api.domain.artist.table.SnsLinkTable
import com.bandchu.api.domain.concert.table.ConcertTable
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
class DatabaseConfig(private val dataSource: DataSource) {

    @EventListener(ContextRefreshedEvent::class)
    fun init() {
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(
                MemberTable,
                ArtiProfileTable,
                SnsLinkTable,
                ConcertTable,
                PostTable,
                CommentTable,
                MediaTable,
                ReportTable,
                SubscriptionTable
            )
        }

        println(" Exposed + Spring Boot 연결 성공")
    }
}
