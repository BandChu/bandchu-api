package com.bandchu.api

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // resources/application-test.yml을 사용하도록 설정
@Testcontainers
abstract class AbstractIntegrationTest {

        companion object {
            private val postgres = PostgreSQLContainer("postgres:15-alpine").apply {
                withDatabaseName("bandchu")
                withUsername("testuser")
                withPassword("testpass")
                start() // 테스트 시작 전 컨테이너 수동 시작
            }

            @JvmStatic
            @DynamicPropertySource
            fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl)
                registry.add("spring.datasource.username", postgres::getUsername)
                registry.add("spring.datasource.password", postgres::getPassword)
                registry.add("spring.flyway.enabled") { "true" } // V1, V2, V3 자동 적용
            }
        }

}