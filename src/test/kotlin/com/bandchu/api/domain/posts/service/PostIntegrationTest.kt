//package com.bandchu.api.domain.posts.service
//
//import com.bandchu.api.AbstractIntegrationTest
//import com.bandchu.api.domain.posts.repository.PostRepository
//import com.bandchu.api.domain.posts.table.PostType
//import com.bandchu.api.domain.posts.dto.request.CreatePostRequest
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.shouldNotBe
//import org.springframework.beans.factory.annotation.Autowired
//
//class PostIntegrationTest(
//    @Autowired private val postService: PostService,
//    @Autowired private val postRepository: PostRepository
//) : AbstractIntegrationTest() { // 위에서 만든 통합 테스트 베이스 상속
//
//    init {
//        describe("Post 서비스 통합 테스트") {
//            context("새로운 게시글을 저장할 때") {
//                it("실제 DB 테이블에 데이터가 영속화되어야 한다") {
//                    // given
//                    val request = CreatePostRequest(
//                        postType = PostType.FREE,
//                        title = "통합 테스트 제목",
//                        content = "실제 DB에 저장되는 본문입니다."
//                    )
//                    val memberId = 1L // 실제 DB에 존재하는 회원 ID (V1에서 생성된 데이터 등)
//
//                    // when
//                    val response = postService.createPost(memberId, request)
//
//                    // then
//                    response.title shouldBe "통합 테스트 제목"
//
//                    // 리포지토리를 통해 실제 DB에 있는지 한 번 더 확인
//                    val savedPost = postRepository.findById(response.id)
//                    savedPost shouldNotBe null
//                }
//            }
//        }
//    }
//}