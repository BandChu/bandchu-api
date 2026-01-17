package com.bandchu.api.domain.subscription

import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.domain.subscription.service.SubscriptionService
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.SubscriptionFixture
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class SubscriptionTestConfig {

    @Bean
    fun authFixture(memberService: MemberService): AuthFixture {
        return AuthFixture(memberService)
    }

    @Bean
    fun subscriptionFixture(
        subscriptionService: SubscriptionService,
        authFixture: AuthFixture
    ): SubscriptionFixture {
        return SubscriptionFixture(subscriptionService, authFixture)
    }
}
