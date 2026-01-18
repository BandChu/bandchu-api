plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.bandchu"
version = "0.0.1-SNAPSHOT"
description = "api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    implementation("org.jetbrains.exposed:exposed-core:1.0.0-rc-2")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-rc-2")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0-rc-2")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("com.ninja-squad:springmockk:4.0.2")
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
    implementation("software.amazon.awssdk:s3:2.25.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.kotest:kotest-runner-junit5:6.0.4")
    testImplementation("io.kotest:kotest-assertions-core:6.0.4")
    testImplementation("io.kotest:kotest-property:6.0.4")
    testImplementation("io.kotest:kotest-extensions-spring:6.0.4")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("com.h2database:h2:2.2.224")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

// Flyway 핵심 라이브러리
    implementation("org.flywaydb:flyway-core")
    // PostgreSQL 전용 의존성
    implementation("org.flywaydb:flyway-database-postgresql")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xjvm-default=all"
        )
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

}


tasks.test {
    // 테스트 클래스 1개당 새로운 JVM 프로세스를 띄움 (완전 격리)
    forkEvery = 1

    // 또는 병렬 실행을 꺼서 하나씩 순차적으로 실행되게 보장
    maxParallelForks = 1

    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
    // JUnit HTML 리포트는 기본적으로 생성됩니다
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    finalizedBy(tasks.jacocoTestReport) // 테스트 실행 후 JaCoCo 리포트 생성
}

// JaCoCo 설정
jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // test 작업 후 실행
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
    }
    // 리포트에 포함할 소스 파일 지정
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/config/**",
                    "**/dto/**",
                    "**/exception/**",
                    "**/global/**",
                    "**/domain/**/dto/**",
                    "**/Application*"
                )
            }
        })
    )
}

// 테스트 커버리지 검증 작업 (선택사항)
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 최소 80% 커버리지 요구
            }
        }
    }

}