# 테스트 리포트 설정 및 커스터마이징

> **사용 가이드**: [테스트 실행 및 리포트 확인 가이드](./TEST_GUIDE.md)를 먼저 확인하세요.

## 빌드 설정

### build.gradle.kts에 포함된 설정

1. **JaCoCo 플러그인**: 코드 커버리지 측정 및 리포트 생성
2. **JUnit HTML 리포트**: 테스트 결과를 HTML로 출력
3. **JaCoCo HTML 리포트**: 코드 커버리지를 HTML로 출력

## 커스터마이징

### 커버리지 제외 패키지 수정

`build.gradle.kts`의 `tasks.jacocoTestReport` 섹션에서 제외할 패키지를 설정할 수 있습니다:

```kotlin
classDirectories.setFrom(
    files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "**/config/**",
                "**/dto/**",
                "**/exception/**",
                // 여기에 추가 패키지를 제외할 수 있습니다
            )
        }
    })
)
```

### 커버리지 최소 기준 변경

`build.gradle.kts`의 `tasks.jacocoTestCoverageVerification` 섹션에서 최소 커버리지 기준을 변경할 수 있습니다:

```kotlin
violationRules {
    rule {
        limit {
            minimum = "0.80".toBigDecimal() // 원하는 비율로 변경 (예: 0.90 = 90%)
        }
    }
}
```

### CSV 리포트 활성화

JaCoCo CSV 리포트가 필요한 경우, `build.gradle.kts`의 `tasks.jacocoTestReport` 섹션에서 활성화할 수 있습니다:

```kotlin
reports {
    csv.required.set(true) // 기본값은 false
}
```
