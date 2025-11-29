# 구독 도메인 개발 관련 공유 문서

## 개요
1차 개발 : 구독 도메인(Subscription Domain)의 아티스트 구독 관련 API를 구현했습니다.

## 구현된 기능

### 1. 아티스트 구독 API
- **엔드포인트**: `POST /api/subscriptions`
- **인증**: Bearer Token 필요
- **기능**: 팬(FAN)이 아티스트를 구독
- **요청 본문**:
  ```json
  {
    "artProfileId": 12
  }
  ```
- **검증**: 
  - 역할 검증: FAN만 구독 가능 (ARTIST는 403 Forbidden)
  - 아티스트 프로필 존재 확인: 존재하지 않는 아티스트 프로필 ID로 구독 시도 시 404 에러
  - 중복 구독 체크: 동일한 회원이 같은 아티스트를 중복 구독할 수 없음
- **응답**:
  ```json
  {
    "success": true,
    "data": {
      "subscriptionId": 88,
      "memberId": 1,
      "artProfileId": 12,
      "createdAt": "2025-11-28T00:00:00Z"
    },
    "message": "아티스트를 구독했습니다."
  }
  ```
- **에러**:
  - 403 Forbidden, `SUBSCRIPTION_INSUFFICIENT_ROLE` (ARTIST 역할 유저가 구독 시도)
  - 404 Not Found, `ARTIST_NOT_FOUND` (존재하지 않는 아티스트 프로필)
  - 409 Conflict, `SUBSCRIPTION_DUPLICATED` (이미 구독 중인 아티스트)
  - 401 Unauthorized, `INVALID_TOKEN` (유효하지 않은 토큰)
  - 403 Forbidden (토큰 없이 요청)

### 2. 구독 취소 API
- **엔드포인트**: `DELETE /api/subscriptions/{artProfileId}`
- **인증**: Bearer Token 필요
- **기능**: 팬(FAN)이 구독 중인 아티스트 구독 취소
- **경로 파라미터**:
  | 이름 | 타입 | 설명 |
  | --- | --- | --- |
  | artProfileId | Long | 구독 취소할 아티스트 ID |
- **응답**:
  ```json
  {
    "success": true,
    "data": null,
    "message": "구독이 취소되었습니다."
  }
  ```
- **에러**:
  - 404 Not Found, `SUBSCRIPTION_NOT_FOUND` (구독 중이 아닌 아티스트)
  - 401 Unauthorized, `INVALID_TOKEN` (유효하지 않은 토큰)
  - 403 Forbidden (토큰 없이 요청)

### 3. 내가 구독한 아티스트 목록 조회 API
- **엔드포인트**: `GET /api/subscriptions`
- **인증**: Bearer Token 필요
- **기능**: 현재 로그인한 사용자가 구독한 아티스트 목록 조회
- **응답**:
  ```json
  {
    "success": true,
    "data": [
      {
        "artProfileId": 1,
        "artistName": "NewJeans",
        "profileImage": "https://example.com/profile.jpg"
      }
    ],
    "message": "구독 목록 조회 성공"
  }
  ```
- **응답 필드 설명**:
  - `artProfileId`: 아티스트 프로필 ID
  - `artistName`: 아티스트 이름 (ArtiProfile에서 조회)
  - `profileImage`: 아티스트 프로필 이미지 URL (ArtiProfile에서 조회, 없으면 빈 문자열)
- **에러**:
  - 401 Unauthorized, `INVALID_TOKEN` (유효하지 않은 토큰)
  - 403 Forbidden (토큰 없이 요청)
- **주의사항**:
  - 아티스트 프로필이 삭제된 경우 해당 구독은 목록에서 제외됨 (조용히 필터링)
  - 아티스트 프로필이 없는 경우 경고 로그 기록

## 수정/생성된 파일

### 구독 도메인 (domain/subscription)

#### Controller
- `src/main/kotlin/com/bandchu/api/domain/subscription/controller/SubscriptionController.kt` (신규)
  - 아티스트 구독 엔드포인트 구현 (`POST /api/subscriptions`)
  - 구독 취소 엔드포인트 구현 (`DELETE /api/subscriptions/{artProfileId}`)
  - 구독 목록 조회 엔드포인트 구현 (`GET /api/subscriptions`)

#### Service
- `src/main/kotlin/com/bandchu/api/domain/subscription/service/SubscriptionService.kt` (신규)
  - `subscribe()`: 아티스트 구독 비즈니스 로직 구현
    - 역할 검증 (FAN만 허용)
    - 아티스트 프로필 존재 확인
    - 중복 구독 체크
  - `unsubscribe()`: 구독 취소 비즈니스 로직 구현
    - 구독 존재 확인 및 삭제
  - `getSubscriptions()`: 구독 목록 조회 비즈니스 로직 구현
    - 구독 목록 조회 후 배치 쿼리로 모든 아티스트 프로필 정보 조회
    - `ArtiProfileRepository.findByIds()`를 사용하여 N+1 쿼리 문제 해결
    - 아티스트 프로필이 없는 경우 경고 로그 기록 및 필터링

#### Repository
- `src/main/kotlin/com/bandchu/api/domain/subscription/repository/SubscriptionRepository.kt` (신규)
  - `save()`: 구독 저장
  - `existsByMemberIdAndArtProfileId()`: 중복 구독 체크
  - `deleteByMemberIdAndArtProfileId()`: 구독 삭제
  - `findByMemberId()`: 회원 ID로 구독 목록 조회

#### DTO
- `src/main/kotlin/com/bandchu/api/domain/subscription/dto/SubscriptionRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/subscription/dto/SubscriptionResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/subscription/dto/SubscriptionListItemResponse.kt` (신규)
  - 구독 목록 조회 응답 DTO (artProfileId, artistName, profileImage)

#### Model
- `src/main/kotlin/com/bandchu/api/domain/subscription/model/Subscription.kt` (신규)
  - 필드: id, memberId, artProfileId, createdAt

#### Table
- `src/main/kotlin/com/bandchu/api/domain/subscription/table/SubscriptionTable.kt` (신규)
  - 컬럼: id, member, art_profile, created_at
  - 외래키: `member` → `MemberTable.id` (CASCADE), `art_profile` → `ArtiProfileTable.id` (CASCADE)
  - UNIQUE 제약조건: (member, art_profile)
  - 공식 문서 기준 `reference()` 방식 사용

#### Test
- `src/test/kotlin/com/bandchu/api/domain/subscription/controller/SubscriptionControllerTest.kt` (신규)
  - 아티스트 구독 테스트 코드 작성
  - 구독 취소 테스트 코드 작성
  - 구독 목록 조회 테스트 코드 작성

### 아티스트 도메인 (domain/artist)

#### Repository
- `src/main/kotlin/com/bandchu/api/domain/artist/repository/ArtiProfileRepository.kt` (수정)
  - `findById()`: 아티스트 프로필 ID로 조회 (구독 시 아티스트 프로필 존재 확인에 사용)
  - `findByIds()`: 여러 아티스트 프로필 ID를 배치로 조회 (구독 목록 조회 시 N+1 쿼리 문제 해결)

### 글로벌 (global)

#### Util
- `src/main/kotlin/com/bandchu/api/global/util/Resolver.kt` (수정)
  - `getCurrentUserRole()`: 현재 로그인한 사용자의 역할(Role) 반환
  - 구독 도메인에서 FAN 역할 검증에 사용

- `src/main/kotlin/com/bandchu/api/global/util/DateTimeConverter.kt` (신규)
  - `LocalDateTime.toOffsetDateTime()`: kotlinx.datetime.LocalDateTime을 java.time.OffsetDateTime으로 변환
  - `OffsetDateTime.toKotlinLocalDateTime()`: java.time.OffsetDateTime을 kotlinx.datetime.LocalDateTime으로 변환
  - 날짜 변환 로직 중복 제거를 위한 유틸리티 함수

#### Config
- `src/main/kotlin/com/bandchu/api/global/config/SecurityConfig.kt` (수정)
  - `/api/subscriptions/**` 엔드포인트 인증 필요로 설정

- `src/main/kotlin/com/bandchu/api/global/config/DatabaseConfig.kt` (수정)
  - `SubscriptionTable` 스키마 생성 추가

#### Exception
- `src/main/kotlin/com/bandchu/api/global/exception/ErrorCode.kt` (수정)
  - `SUBSCRIPTION_DUPLICATED`: 이미 구독 중인 아티스트 에러
  - `SUBSCRIPTION_NOT_FOUND`: 구독 중이 아닌 아티스트 에러
  - `SUBSCRIPTION_INSUFFICIENT_ROLE`: 팬만 구독할 수 있음 에러 (기존 INVALID_ROLE에서 변경)

## curl 테스트 결과

### 1. 아티스트 구독 API
```bash
POST /api/subscriptions
Authorization: Bearer {accessToken}
Content-Type: application/json
{
  "artProfileId": 12
}
```
- ✅ **성공 케이스**: 201 Created, 구독 정보 반환 (subscriptionId, memberId, artProfileId, createdAt)
- ✅ **실패 케이스 (중복 구독)**: 409 Conflict, `SUBSCRIPTION_DUPLICATED` 에러
- ✅ **실패 케이스 (존재하지 않는 아티스트)**: 404 Not Found, `ARTIST_NOT_FOUND` 에러
- ✅ **실패 케이스 (ARTIST 역할)**: 403 Forbidden, `SUBSCRIPTION_INSUFFICIENT_ROLE` 에러
- ✅ **실패 케이스 (토큰 없음)**: 403 Forbidden (Spring Security 기본 동작)
- ✅ **실패 케이스 (유효하지 않은 토큰)**: 401 Unauthorized, `INVALID_TOKEN` 에러

### 2. 구독 취소 API
```bash
DELETE /api/subscriptions/{artProfileId}
Authorization: Bearer {accessToken}
```
- ✅ **성공 케이스**: 200 OK, 성공 메시지 반환
- ✅ **실패 케이스 (구독 없음)**: 404 Not Found, `SUBSCRIPTION_NOT_FOUND` 에러
- ✅ **실패 케이스 (토큰 없음)**: 403 Forbidden (Spring Security 기본 동작)
- ✅ **실패 케이스 (유효하지 않은 토큰)**: 401 Unauthorized, `INVALID_TOKEN` 에러

### 3. 내가 구독한 아티스트 목록 조회 API
```bash
GET /api/subscriptions
Authorization: Bearer {accessToken}
```
- ✅ **성공 케이스**: 200 OK, 구독 목록 반환 (artProfileId, artistName, profileImage)
- ✅ **성공 케이스 (구독 없음)**: 200 OK, 빈 배열 반환
- ✅ **실패 케이스 (토큰 없음)**: 403 Forbidden (Spring Security 기본 동작)
- ✅ **실패 케이스 (유효하지 않은 토큰)**: 401 Unauthorized, `INVALID_TOKEN` 에러

## 테스트 상태

### 단위 테스트
- ✅ 테스트 코드 작성 완료 (`SubscriptionControllerTest.kt`)
  - 아티스트 구독 테스트
  - 중복 구독 테스트
  - 존재하지 않는 아티스트에 구독 요청 테스트
  - ARTIST 역할 유저 구독 시도 테스트
  - 토큰 없이 요청 테스트
  - 구독 취소 테스트
  - 구독 중이 아닌 아티스트 구독 취소 시도 테스트
  - 구독 목록 조회 테스트
  - 구독 목록이 비어있는 경우 테스트
- ❌ **아직 테스트 실행은 하지 않음**

### 통합 테스트 (curl)
- ✅ 모든 API curl 테스트 완료
- ✅ 정상 동작 확인
- ✅ 아티스트 도메인 연동 확인 (ArtiProfileRepository.findById() 사용)
- ✅ 아티스트 정보 매핑 확인 (artistName, profileImageUrl)

## 주의!!! 데이터베이스 변경사항

**버전 1.0에서 `subscriptions` 테이블이 새로 생성되었습니다.**  
**버전 1.4에서 외래키 제약조건이 추가되었습니다.**

### 변경 내용 (버전 1.4)
- 외래키 제약조건 추가
  - `member` → `members.id` (ON DELETE CASCADE)
  - `art_profile` → `arti_profiles.id` (ON DELETE CASCADE)
- 컬럼명 변경: `member_id` → `member`, `art_profile_id` → `art_profile`

### 변경 내용 (버전 1.0)
- `subscriptions` 테이블 생성
  - `id`: BIGSERIAL PRIMARY KEY
  - `member`: BIGINT NOT NULL (외래키: `members.id`)
  - `art_profile`: BIGINT NOT NULL (외래키: `arti_profiles.id`)
  - `created_at`: TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
  - UNIQUE 제약조건: `(member, art_profile)`
  - 외래키 제약조건: `member` → `members.id` (CASCADE), `art_profile` → `arti_profiles.id` (CASCADE)

### 주의사항
- 기존 데이터베이스를 사용하는 경우, **반드시 마이그레이션을 실행**해야 합니다.
- 마이그레이션을 실행하지 않으면 아티스트 구독 API가 동작하지 않습니다.
- 버전 1.4부터는 외래키 제약조건이 추가되어 부모 레코드 삭제 시 자식 레코드도 자동 삭제됩니다.
- 마이그레이션 SQL (버전 1.4):
  ```sql
  CREATE TABLE IF NOT EXISTS subscriptions (
      id BIGSERIAL PRIMARY KEY,
      member BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
      art_profile BIGINT NOT NULL REFERENCES arti_profiles(id) ON DELETE CASCADE,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
      UNIQUE(member, art_profile)
  );
  ```

## TODO

1. **TDD 테스트 실행**
   - `SubscriptionControllerTest.kt` 실행하여 테스트 통과 확인

## 보안 고려사항

1. **역할 기반 접근 제어**
   - FAN 역할만 구독 가능하도록 검증
   - ARTIST 역할 유저는 구독 불가

2. **중복 구독 방지**
   - 데이터베이스 UNIQUE 제약조건으로 중복 구독 방지
   - 애플리케이션 레벨에서도 중복 체크 수행

---

**작성일**: 2025-11-28  
**최종 수정일**: 2025-11-28  
**작성자**: 신진수  
**버전**: 1.4

## 변경 이력

### 버전 1.4 (2025-11-28)
- 외래키 참조 방식 통일: `SubscriptionTable`의 외래키를 공식 문서 기준인 `reference()` 방식으로 변경
- `onDelete = ReferenceOption.CASCADE` 옵션 추가로 부모 레코드 삭제 시 자식 레코드 자동 삭제
- `MemberTable`을 `LongIdTable`로 변경하여 `EntityID<Long>` 타입 통일
- `SubscriptionRepository`에서 외래키 컬럼 접근 시 `.value` 사용으로 타입 일관성 확보
- 아티스트 도메인과 동일한 네이밍 컨벤션 적용 (`member`, `art_profile`)

### 버전 1.3 (2025-11-28)
- 아티스트 프로필 존재 확인 추가 (구독 시 존재하지 않는 아티스트 프로필 ID로 구독 시도 방지)
- N+1 쿼리 문제 해결: `ArtiProfileRepository.findByIds()` 메서드 추가로 구독 목록 조회 성능 개선
- 에러 코드 개선: `INVALID_ROLE` → `SUBSCRIPTION_INSUFFICIENT_ROLE`로 변경 (도메인별 네이밍 일관성)
- 날짜 변환 유틸리티 함수 추가: `DateTimeConverter.kt` 생성으로 코드 중복 제거
- 테스트 케이스 추가: 존재하지 않는 아티스트에 구독 요청 시나리오 추가

### 버전 1.2 (2025-11-28)
- 내가 구독한 아티스트 목록 조회 API 추가 (`GET /api/subscriptions`)
- 구독 목록 조회 기능 구현
- `SubscriptionRepository.findByMemberId()` 메서드 추가
- `SubscriptionService.getSubscriptions()` 메서드 추가
- `SubscriptionListItemResponse` DTO 추가
- 아티스트 도메인 연동 (`ArtiProfileRepository.findById()` 사용)
- 아티스트 프로필이 없는 경우 경고 로그 기록 및 필터링 처리
- 구독 목록 조회 테스트 코드 작성 및 curl 테스트 완료

### 버전 1.1 (2025-11-28)
- 구독 취소 API 추가 (`DELETE /api/subscriptions/{artProfileId}`)
- 구독 취소 기능 구현
- `SubscriptionRepository.deleteByMemberIdAndArtProfileId()` 메서드 추가
- `SubscriptionService.unsubscribe()` 메서드 추가
- ErrorCode에 `SUBSCRIPTION_NOT_FOUND` 추가
- 구독 취소 테스트 코드 작성 및 curl 테스트 완료

### 버전 1.0 (2025-11-28)
- 아티스트 구독 API 추가 (`POST /api/subscriptions`)
- 아티스트 구독 기능 구현
- 역할 검증 구현 (FAN만 허용)
- 중복 구독 방지 구현 (UNIQUE 제약조건)
- Subscription 모델 및 테이블 생성
- SubscriptionRepository, SubscriptionService, SubscriptionController 구현
- ErrorCode에 `SUBSCRIPTION_DUPLICATED`, `SUBSCRIPTION_INSUFFICIENT_ROLE` 추가
- 아티스트 구독 테스트 코드 작성 및 curl 테스트 완료

