package com.bandchu.api

import com.bandchu.api.domain.member.service.GoogleOAuthService
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.SubscriptionFixture
import com.bandchu.api.global.config.ConfigController
import com.bandchu.api.global.config.S3Uploader
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import com.ninjasquad.springmockk.MockkBean
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiApplicationTests {
    @MockkBean lateinit var s3Uploader: S3Uploader
    @MockkBean lateinit var googleOAuthService: GoogleOAuthService
    @MockkBean lateinit var configController: ConfigController
    @MockkBean(relaxed = true) lateinit var authFixture: AuthFixture
    @MockkBean(relaxed = true) lateinit var subscriptionFixture: SubscriptionFixture
    @Test
    fun contextLoads() {
    }

}
