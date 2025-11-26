package com.bandchu.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import java.util.*

@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    // .env 파일을 읽어서 시스템 프로퍼티로 설정
    // application.yml의 ${DB_URL} 같은 플레이스홀더가 이를 참조할 수 있음
    val envFile = File(".env")
    if (envFile.exists()) {
        val properties = Properties()
        envFile.inputStream().use { input ->
            properties.load(input)
        }
        properties.forEach { key, value ->
            // 환경 변수가 이미 설정되어 있지 않은 경우에만 .env 파일의 값을 사용
            if (System.getenv(key.toString()) == null) {
                System.setProperty(key.toString(), value.toString())
            }
        }
    }
    
    runApplication<ApiApplication>(*args)
}