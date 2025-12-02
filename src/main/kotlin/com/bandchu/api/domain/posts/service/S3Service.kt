package com.bandchu.api.domain.posts.service

import com.bandchu.api.global.config.S3Uploader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class S3Service(
    private val s3Uploader: S3Uploader
) {

    fun uploadMedia(postId: Long, file: MultipartFile): String {
        return s3Uploader.upload(file, "posts/$postId")
    }

    fun deleteMedia(key: String) {
        s3Uploader.delete(key)
    }
}
