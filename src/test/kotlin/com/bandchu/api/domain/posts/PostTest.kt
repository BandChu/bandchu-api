package com.bandchu.api.domain.posts

import com.bandchu.api.domain.posts.repository.PostRepository
import com.bandchu.api.domain.posts.service.PostService
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.context.SpringBootTest
// Kotest 기본 양식
@SpringBootTest
class PostServiceTest(
    private val postRepository: PostRepository,
    private val postService: PostService
) : BehaviorSpec({

    given("게시글을 저장할 때") {

        val title = "My Post"
        val content = "Content here"

        `when`("PostService.save를 호출하면") {

            val id = postService.save(title, content)

            then("DB에 저장되고 ID를 반환한다") {
                val saved = postRepository.findById(id).orElseThrow()
                saved.title shouldBe title
                saved.content shouldBe content
            }
        }
    }
})
