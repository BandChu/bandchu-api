package com.bandchu.api.domain.chat

import com.bandchu.api.domain.chat.service.ChatMessageService
import com.bandchu.api.domain.chat.service.ChatRoomService
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.ChatFixture
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.messaging.simp.SimpMessagingTemplate

@TestConfiguration
class ChatTestConfig {

    @Bean
    fun authFixture(memberService: MemberService): AuthFixture {
        return AuthFixture(memberService)
    }

    @Bean
    fun chatFixture(
        chatRoomService: ChatRoomService,
        chatMessageService: ChatMessageService,
        authFixture: AuthFixture
    ): ChatFixture {
        return ChatFixture(chatRoomService, chatMessageService, authFixture)
    }

    @Bean
    fun simpMessagingTemplate(): SimpMessagingTemplate {
        return mockk(relaxed = true)
    }
}
