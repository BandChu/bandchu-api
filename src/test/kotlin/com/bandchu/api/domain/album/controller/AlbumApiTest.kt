package com.bandchu.api.domain.album.controller

import com.bandchu.api.domain.album.AlbumTestConfig
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

/**
 * Album API 통합 테스트
 *
 * - MockMvc 기반으로 Controller–Service–Repository 흐름을 검증합니다.
 * - 실제 JWT 검증은 수행하지 않으며,
 *   SecurityMockMvcRequestPostProcessors.user()를 사용해
 *   SecurityContext에 인증 정보를 직접 주입하여 인증/인가를 테스트합니다.
 */

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(AlbumTestConfig::class)
class AlbumApiTest {
}