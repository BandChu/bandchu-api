import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.member.service.GoogleOAuthService
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.domain.member.dto.LoginRequest
import com.bandchu.api.global.security.JwtService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class MemberServiceTest {

    private val memberRepository = mockk<MemberRepository>()
    private val jwtService = mockk<JwtService>()
    private val googleOAuthService = mockk<GoogleOAuthService>()
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
    private val memberService = MemberService(memberRepository, jwtService, googleOAuthService, passwordEncoder)

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `존재하지 않는 이메일로 로그인 요청이 들어오면 USER_INVALID_CREDENTIAL 예외를 발생시킨다`() {
        // given
        val request = LoginRequest(
            email = "notfound@example.com",
            password = "password123"
        )

        every { memberRepository.findByEmail("notfound@example.com") } returns null

        // when & then
        try {
            memberService.login(request)
            assert(false) { "Expected BusinessException to be thrown" }
        } catch (e: com.bandchu.api.global.exception.BusinessException) {
            assertEquals(com.bandchu.api.global.exception.ErrorCode.USER_INVALID_CREDENTIAL, e.errorCode)
        }
    }

    @Test
    fun `잘못된 비밀번호로 로그인 요청이 들어오면 USER_INVALID_CREDENTIAL 예외를 발생시킨다`() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        // BCrypt로 해시화된 비밀번호 생성 (잘못된 비밀번호와 다른 해시)
        val correctPassword = "correctpassword"
        val hashedPassword = passwordEncoder.encode(correctPassword)
        
        val existingMember = Member(
            id = 1L,
            email = "test@example.com",
            password = hashedPassword,
            nickname = "테스트유저",
            role = Role.FAN,
            createdAt = LocalDateTime(2024, Month.JANUARY, 1, 0, 0, 0)
        )

        every { memberRepository.findByEmail("test@example.com") } returns existingMember

        // when & then
        try {
            memberService.login(request)
            assert(false) { "Expected BusinessException to be thrown" }
        } catch (e: com.bandchu.api.global.exception.BusinessException) {
            assertEquals(com.bandchu.api.global.exception.ErrorCode.USER_INVALID_CREDENTIAL, e.errorCode)
        }
    }

    @Test
    fun `유효한 리프레시 토큰으로 재발급 요청이 들어오면 새로운 토큰 쌍을 반환한다`() {
        // given
        val refreshToken = "valid.refresh.token"
        val memberId = 1L
        val role = Role.FAN

        every { jwtService.validateToken(refreshToken) } returns true
        every { jwtService.getTokenTypeFromToken(refreshToken) } returns "refresh"
        every { jwtService.getMemberIdFromToken(refreshToken) } returns memberId
        every { jwtService.getRoleFromToken(refreshToken) } returns role

        val member = Member(
            id = memberId,
            email = "test@example.com",
            password = "password",
            nickname = "테스트유저",
            role = role,
            createdAt = LocalDateTime(2024, Month.JANUARY, 1, 0, 0, 0)
        )
        every { memberRepository.findById(memberId) } returns member

        every { jwtService.generateAccessToken(memberId, role) } returns "new.access.token"
        every { jwtService.generateRefreshToken(memberId, role) } returns "new.refresh.token"

        // when
        val result = memberService.refreshToken(refreshToken)

        // then
        assertNotNull(result)
        assertEquals("new.access.token", result.accessToken)
        assertEquals("new.refresh.token", result.refreshToken)
    }
}