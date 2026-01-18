package com.bandchu.api.fixture

import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.member.service.MemberService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class AuthFixture(
    private val memberService: MemberService,
) {
    /**
     * 외부 환경에 대한 의존을 줄이고, 재사용 가능한 코드를 만들기 위한 테스트 객체 생성 Fixture입니다.
     * Member를 생성하고, 생성된 Member를 객체를 반환합니다.
     * 현재 테스트 스레드의 Security Context에 인증 정보를 주입합니다.
     */

    data class AuthCredentials(val email: String, val password: String, val nickname: String, val role: Role)

    fun createMember(
        credentials: AuthCredentials,
    ): Member {

        return memberService.signup(
            SignupRequest(
                email = credentials.email,
                password = credentials.password,
                nickname = credentials.nickname,
                role = credentials.role
            )
        ).member
    }

    fun authenticateAs(member: Member) {
        val authorities: List<GrantedAuthority> = listOf(
            SimpleGrantedAuthority("ROLE_" + member.role.name)
        )

        val auth = UsernamePasswordAuthenticationToken(
            member.id,
            null,
            authorities
        )

        SecurityContextHolder.getContext().authentication = auth
    }
}