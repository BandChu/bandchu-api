# 테스트 실행 및 리포트 확인

## 빠른 시작

```bash
# 테스트 실행
./gradlew test

# 리포트 요약 확인
./scripts/show-test-report.sh

# 리포트 자동으로 브라우저에서 열기
./scripts/show-test-report.sh --open
```

## 실무 워크플로우

```bash
# 테스트 실행 + 리포트 확인 (한 줄)
./gradlew test && ./scripts/show-test-report.sh

# 실패한 테스트 상세 확인 (자동으로 브라우저 열기)
./gradlew test && ./scripts/show-test-report.sh --open
```

## 특정 테스트만 실행

```bash
# 특정 클래스의 테스트만 실행
./gradlew test --tests "com.bandchu.api.domain.member.model.MemberTest"

# 특정 패키지의 테스트만 실행
./gradlew test --tests "com.bandchu.api.domain.member.*"
```

## 참고
- [테스트 리포트 설정 및 커스터마이징](./TEST_REPORTS.md) - 빌드 설정 변경이 필요한 경우
