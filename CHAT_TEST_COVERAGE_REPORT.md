# 채팅 도메인 테스트 커버리지 리포트

## 📊 전체 요약

작성일: 2026-01-18

### 테스트 통계

| 계층 | 테스트 파일 수 | 테스트 케이스 수 | 상태 |
|------|---------------|----------------|------|
| **Controller (통합)** | 2 | 18 | ✅ 전부 통과 |
| **Service (유닛)** | 2 | 13 | ✅ 전부 통과 |
| **Repository (유닛)** | 3 | - | ⚠️ 일부 실패 (트랜잭션 이슈) |
| **총합** | 7 | 31+ | 통합/Service 테스트 통과 |

## 📈 코드 커버리지 (Jacoco)

### Controller 계층
- **라인 커버리지**: 100% (96/96 instructions)
- **브랜치 커버리지**: N/A
- **메서드 커버리지**: 100% (7/7 methods)
- **클래스 커버리지**: 100% (2/2 classes)

**파일:**
- `ChatRoomController`: 100% (56 instructions)
- `ChatMessageController`: 100% (40 instructions)

### Service 계층
- **라인 커버리지**: 94% (484/510 instructions)
- **브랜치 커버리지**: 79% (39/49 branches)
- **메서드 커버리지**: 100% (11/11 methods)
- **클래스 커버리지**: 100% (3/3 classes)

**파일:**
- `ChatMessageService`: 100% (100 instructions, 8/8 branches)
- `ChatRoomService`: 93% (369/395 instructions, 31/41 branches)

### Repository 계층
- **라인 커버리지**: 91% (837/919 instructions)
- **브랜치 커버리지**: 64% (9/14 branches)
- **메서드 커버리지**: 92% (48/52 methods)
- **클래스 커버리지**: 100% (3/3 classes)

**파일:**
- `ChatRoomRepository`: 98% (152 instructions, 3/4 branches)
- `MemberChatRoomRepository`: 96% (386 instructions, 1/2 branches)
- `ChatMessageRepository`: 81% (299 instructions, 5/8 branches)

## 📝 작성된 테스트 파일

### 1. 통합 테스트 (Controller)

#### ChatRoomApiTest
```
src/test/kotlin/com/bandchu/api/domain/chat/controller/ChatRoomApiTest.kt
```

**테스트 시나리오:**
- ✅ DIRECT 채팅방 생성 후 목록 조회
- ✅ GROUP 채팅방 생성 후 멤버 정보 확인
- ✅ 이미 존재하는 DIRECT 채팅방 생성 시도 (멱등성)
- ✅ 메시지 전송 후 읽음 처리
- ✅ Edge Cases:
  - DIRECT 채팅방에 2명 이상 초대 시도
  - DIRECT 채팅방에 memberIds 비어있음
  - 존재하지 않는 사용자 초대
  - 비참여자의 읽음 상태 업데이트
  - 존재하지 않는 채팅방의 읽음 상태 업데이트
  - 대규모 그룹 채팅방 생성

#### ChatMessageApiTest
```
src/test/kotlin/com/bandchu/api/domain/chat/controller/ChatMessageApiTest.kt
```

**테스트 시나리오:**
- ✅ TEXT/IMAGE 메시지 전송 후 조회
- ✅ 커서 기반 페이징으로 대량 메시지 조회
- ✅ 빈 채팅방 메시지 조회
- ✅ Edge Cases:
  - 채팅방 비참여자의 메시지 전송 시도
  - 존재하지 않는 채팅방에 메시지 전송
  - TEXT 타입인데 content가 null
  - 커서 값이 음수
  - size가 0

### 2. 유닛 테스트 (Service)

#### ChatRoomServiceTest
```
src/test/kotlin/com/bandchu/api/domain/chat/service/ChatRoomServiceTest.kt
```

**테스트 시나리오:**
- ✅ 1:1 채팅방 생성 (신규/기존 분기)
- ✅ 그룹 채팅방 생성
- ✅ 채팅방 목록 조회 (빈 목록/읽지 않은 메시지)
- ✅ 읽음 처리 (정상/비참여자/존재하지 않는 방)

#### ChatMessageServiceTest
```
src/test/kotlin/com/bandchu/api/domain/chat/service/ChatMessageServiceTest.kt
```

**테스트 시나리오:**
- ✅ TEXT 메시지 저장 및 WebSocket 브로드캐스트
- ✅ IMAGE 메시지 저장 및 WebSocket 브로드캐스트
- ✅ 채팅방 비참여자 예외 처리
- ✅ 존재하지 않는 채팅방 예외 처리
- ✅ 메시지 조회 (커서 없음/있음/빈 결과/커스텀 size)

### 3. 유닛 테스트 (Repository)

#### ChatRoomRepositoryTest
```
src/test/kotlin/com/bandchu/api/domain/chat/repository/ChatRoomRepositoryTest.kt
```

**테스트 시나리오:**
- DIRECT/GROUP 채팅방 생성
- ID로 채팅방 조회
- 여러 ID로 채팅방 조회 (빈 리스트/일부 존재/중복 제거)
- 생성 시간 검증

#### ChatMessageRepositoryTest
```
src/test/kotlin/com/bandchu/api/domain/chat/repository/ChatMessageRepositoryTest.kt
```

**테스트 시나리오:**
- TEXT/IMAGE 메시지 저장
- 채팅방 참여자 확인
- 커서 기반 메시지 조회
- 마지막 메시지 조회
- 읽지 않은 메시지 개수 계산
- 전체 메시지 개수 계산 (본인 제외)

#### MemberChatRoomRepositoryTest
```
src/test/kotlin/com/bandchu/api/domain/chat/repository/MemberChatRoomRepositoryTest.kt
```

**테스트 시나리오:**
- 채팅방에 회원 추가
- 회원이 속한 채팅방 ID 조회
- 채팅방의 모든 참여자 ID 조회
- 읽음 상태 업데이트
- 마지막 읽은 메시지 ID 조회
- 두 사용자 간 공통 1:1 채팅방 찾기
- 채팅방의 멤버 정보 조회

## 🎯 커버리지 개선 효과

| 항목 | 이전 | 현재 | 개선 |
|------|------|------|------|
| **Controller 테스트** | 통합 테스트만 존재 | 통합 + Edge Cases | +6개 시나리오 |
| **Service 테스트** | 1개 (disabled) | 2개 (13 케이스) | +2파일, +13케이스 |
| **Repository 테스트** | 없음 | 3개 작성 | +3파일 |
| **전체 라인 커버리지** | ~40-50% 추정 | **Controller 100%, Service 94%, Repository 91%** | +40-50%p |

## ✅ 통과한 테스트

```bash
./gradlew test --tests "com.bandchu.api.domain.chat.controller.*" --tests "com.bandchu.api.domain.chat.service.*"
```

**결과**: BUILD SUCCESSFUL (31개 테스트 통과)

## 🔧 테스트 인프라

### ChatTestConfig
- WebSocket의 `SimpMessagingTemplate`을 Mock으로 대체
- 실제 WebSocket 연결 없이 테스트 가능

### ChatFixture
테스트용 헬퍼 클래스:
- `createDirectRoom()`: 1:1 채팅방 생성
- `createGroupRoom()`: 그룹 채팅방 생성
- `sendMessage()`: 메시지 전송
- `sendMultipleMessages()`: 대량 메시지 생성

### AuthFixture
- 테스트용 회원 생성 및 인증 처리

## 📌 주요 개선사항

1. **Mock 관리 개선**: `clearMocks()`를 사용하여 테스트 간 상태 격리
2. **트랜잭션 관리**: Repository 테스트에 `@Transactional` 설정
3. **Edge Case 커버리지**: 예외 상황 및 경계값 테스트 추가
4. **Jacoco 통합**: 자동 커버리지 측정 및 리포트 생성

## 🚀 실행 방법

### 전체 테스트 실행
```bash
./gradlew test --tests "com.bandchu.api.domain.chat.*"
```

### 커버리지 리포트 생성
```bash
./gradlew test jacocoTestReport
```

### 커버리지 리포트 확인
```
build/reports/jacoco/test/html/index.html
```

### 채팅 도메인만 테스트
```bash
./gradlew test --tests "com.bandchu.api.domain.chat.controller.*" --tests "com.bandchu.api.domain.chat.service.*"
```

## 📊 커버리지 검증 규칙

### 설정된 최소 커버리지 기준
- **라인 커버리지**: 70% 이상
- **브랜치 커버리지**: 60% 이상

### 제외 항목
- `**/config/**`
- `**/dto/**`
- `**/table/**`
- `**/model/**`
- `**/*Application*`
- `**/*Config*`

## 💡 향후 개선 과제

1. ⚠️ Repository 유닛 테스트 트랜잭션 이슈 해결
2. 📈 브랜치 커버리지 향상 (현재 64-79% → 목표 85%+)
3. 🧪 WebSocket 실제 통신 테스트 추가
4. 📝 성능 테스트 추가 (대량 메시지 처리)
5. 🔒 보안 테스트 추가 (권한 검증 강화)

## 📄 관련 문서

- [Jacoco Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Kotest Documentation](https://kotest.io/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
