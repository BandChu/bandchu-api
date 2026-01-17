package com.bandchu.api.domain.member.model

import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MemberTest : DescribeSpec({

    describe("verifyPassword") {
        context("올바른 비밀번호로 검증하면") {
            it("예외가 발생하지 않는다") {
                // given
                val member = Member(
                    email = "test@example.com",
                    password = "password123",
                    nickname = "테스트유저",
                    role = Role.FAN
                )

                // when & then
                member.verifyPassword("password123") // 예외 발생하지 않음
            }
        }

        context("잘못된 비밀번호로 검증하면") {
            it("USER_INVALID_CREDENTIAL 예외를 발생시킨다") {
                // given
                val member = Member(
                    email = "test@example.com",
                    password = "password123",
                    nickname = "테스트유저",
                    role = Role.FAN
                )

                // when & then
                val exception = shouldThrow<BusinessException> {
                    member.verifyPassword("wrongpassword")
                }

                exception.errorCode shouldBe ErrorCode.USER_INVALID_CREDENTIAL
            }
        }
    }

    describe("createForSignup") {
        context("일반 회원가입용 회원을 생성하면") {
            it("프로필 완료 상태로 생성된다") {
                // when
                val member = Member.createForSignup(
                    email = "test@example.com",
                    password = "password123",
                    nickname = "테스트유저",
                    role = Role.FAN
                )

                // then
                member.email shouldBe "test@example.com"
                member.password shouldBe "password123"
                member.nickname shouldBe "테스트유저"
                member.role shouldBe Role.FAN
                member.isProfileCompleted shouldBe true
                member.googleId shouldBe null
                member.profileImageUrl shouldBe null
            }

            it("ARTIST 역할로도 생성할 수 있다") {
                // when
                val member = Member.createForSignup(
                    email = "artist@example.com",
                    password = "password123",
                    nickname = "아티스트",
                    role = Role.ARTIST
                )

                // then
                member.role shouldBe Role.ARTIST
                member.isProfileCompleted shouldBe true
            }
        }
    }

    describe("createForOAuth") {
        context("OAuth 회원가입용 회원을 생성하면") {
            it("프로필 미완료 상태로 생성되고 기본 역할은 FAN이다") {
                // when
                val member = Member.createForOAuth(
                    email = "oauth@example.com",
                    nickname = "구글유저",
                    googleId = "google-id-123"
                )

                // then
                member.email shouldBe "oauth@example.com"
                member.password shouldBe "" // OAuth 회원은 비밀번호 없음
                member.nickname shouldBe "구글유저"
                member.role shouldBe Role.FAN // 기본 역할은 FAN
                member.googleId shouldBe "google-id-123"
                member.isProfileCompleted shouldBe false // 프로필 미완료 상태
                member.profileImageUrl shouldBe null
            }
        }
    }
})
