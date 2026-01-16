package com.bandchu.api.global.config

import com.bandchu.api.global.config.S3Properties
import com.bandchu.api.global.exception.BusinessException
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
        val originalName = file.originalFilename ?: "unknown"
        val fileName = generateFileName(originalName)
        val key = "$dir/$fileName"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Properties.bucket)
            .key(key)
            .contentType(file.contentType ?: "application/octet-stream")
            .build()

        file.inputStream.use { inputStream ->
            s3Client.putObject(
                putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, file.size)
            )
        }

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
