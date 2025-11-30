package com.bandchu.api.global.s3

import com.bandchu.api.global.config.S3Properties
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class S3Uploader(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties
) {

    fun upload(file: MultipartFile, dir: String): String {

        val fileName = generateFileName(file.originalFilename!!)
        val key = "$dir/$fileName"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Properties.bucket)
            .key(key)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.bytes))

        return getUrl(key)
    }

    fun delete(key: String) {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(s3Properties.bucket)
            .key(key)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }

    private fun generateFileName(original: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        return "$timestamp-${original.replace(" ", "_")}"
    }

    private fun getUrl(key: String): String =
        "https://${s3Properties.bucket}.s3.${s3Properties.region}.amazonaws.com/$key"
}
