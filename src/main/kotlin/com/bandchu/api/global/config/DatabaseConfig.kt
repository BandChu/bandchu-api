package com.bandchu.api.global.config

import io.github.cdimascio.dotenv.Dotenv

import org.jetbrains.exposed.v1.jdbc.transactions.transaction

import org.jetbrains.exposed.v1.jdbc.Database
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DatabaseConfig {

    @Bean
    open fun initDatabase(): Database {
        val dotenv = Dotenv.configure()
            .directory("/Users/sonhyeonbin/Downloads/bandchu")
            .load()

        val url = dotenv["DB_URL"] ?: error("DB_URL missing")
        val username = dotenv["DB_USERNAME"] ?: error("DB_USERNAME missing")
        val password = dotenv["DB_PASSWORD"] ?: error("DB_PASSWORD missing")

        val db = Database.connect(
            url = url,
            driver = "org.postgresql.Driver",
            user = username,
            password = password
        )

        // 🔥 Exposed v1 DB 연결 테스트
        transaction(db) {
            exec("SELECT 1") {
                println("🔥🔥🔥 DB 연결 성공!!")
            }
        }

        return db
    }
}
