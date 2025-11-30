package com.bandchu.api.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "aws")
class S3Properties {
    lateinit var s3: S3Bucket
    lateinit var credentials: Credentials
    lateinit var region: String

    class S3Bucket {
        lateinit var bucket: String
    }

    class Credentials {
        lateinit var accessKey: String
        lateinit var secretKey: String
    }

    val bucket: String get() = s3.bucket
    val accessKey: String get() = credentials.accessKey
    val secretKey: String get() = credentials.secretKey
}
