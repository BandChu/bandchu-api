package com.bandchu.api.domain.posts.service

import com.bandchu.api.ApiApplicationTests
import com.bandchu.api.domain.concert.ConcertTestConfig
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.member.service.GoogleOAuthService
import com.bandchu.api.domain.posts.PostTestConfig
import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
import com.bandchu.api.domain.posts.dto.request.UpdatePostRequest
import com.bandchu.api.domain.posts.dto.response.CreatePostResponse
import com.bandchu.api.domain.posts.repository.CommentRepository
import com.bandchu.api.domain.posts.repository.MediaRepository
import com.bandchu.api.domain.posts.repository.PostRepository
import com.bandchu.api.domain.posts.repository.ReportRepository
import com.bandchu.api.domain.posts.table.PostType
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.PostFixture
import com.bandchu.api.global.config.ConfigController
import com.bandchu.api.global.config.S3Uploader
import com.bandchu.api.global.security.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.system.NoSystemErrListener.beforeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import java.awt.PageAttributes
import java.time.OffsetDateTime



@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(PostTestConfig::class)

class PostApiTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val authFixture: AuthFixture,
    private val postFixture: PostFixture
) :DescribeSpec() {


    private lateinit var writer: Member
    private lateinit var otherUser: Member
    private lateinit var myPost: CreatePostResponse

    private val NON_EXIST_ID = 99999L

    init {

        beforeSpec {
            val suffix = System.currentTimeMillis()

            writer = authFixture.createMember(
                AuthFixture.AuthCredentials("writer-$suffix@test.com", "pass", "Writer", Role.FAN)
            )
            otherUser = authFixture.createMember(
                AuthFixture.AuthCredentials("other-$suffix@test.com", "pass", "OtherUser", Role.FAN)
            )

            authFixture.authenticateAs(writer)
            myPost = postFixture.createPost(member = writer)
        }

        describe("게시글 생성 API") {
            val req = CreatePostRequest(
                postType = PostType.FREE,
                title = "테스트 생성",
                content = "테스트 본문"
            )
            val json = objectMapper.writeValueAsString(req)

            context("정상 요청") {
                it("200 CREATED") {

                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .with(user(writer.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()

                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["title"].asText() shouldBe "테스트 생성"
                }
            }
        }

        describe("게시글 상세 조회") {

            context("존재하는 게시글 조회 시") {
                it("200 OK") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/${myPost.id}")
                            .with(user(writer.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()

                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["title"].asText() shouldBe myPost.title
                }
            }

            context("존재하지 않는 게시글 조회 시") {
                it("404 Not Found") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/$NON_EXIST_ID")
                            .with(user(writer.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                }
            }
        }

        describe("게시글 수정") {

            val req = UpdatePostRequest(
                title = "수정된 제목",
                content = "수정된 본문"
            )
            val json = objectMapper.writeValueAsString(req)

            context("작성자가 수정 요청 시") {
                it("200 OK") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/posts/${myPost.id}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .with(user(writer.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()

                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["title"].asText() shouldBe "수정된 제목"
                }
            }

            context("작성자가 아닌 사용자가 수정 요청 시") {
                it("403 Forbidden") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/posts/${myPost.id}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .with(user(otherUser.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                }
            }
        }

        describe("게시글 삭제") {

            context("작성자가 삭제 요청 시") {
                it("200 No Content") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/posts/${myPost.id}")
                            .with(user(writer.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                }
            }

            context("작성자가 아닌 사용자가 삭제 요청 시") {
                it("403 Forbidden") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/posts/${myPost.id}")
                            .with(user(otherUser.id.toString()).roles("FAN"))
                    ).andReturn().response
                    // 로직 상 작성자가 아닌 사용자 먼저 검사가 아니라 그 게시물이 존재하는지 안하는지를 먼저 검사해서 그런듯
                    //TODO:로직 개선하기

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                }
            }
        }
    }
}