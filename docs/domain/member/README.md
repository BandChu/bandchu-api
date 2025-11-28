# 회원 도메인 개발 관련 공유 문서

## 개요
1차 개발 : 회원 도메인(Member Domain)의 인증 관련 API를 구현했습니다.

## 구현된 기능

### 1. 회원 가입 API
- **엔드포인트**: `POST /api/members/signup`
- **기능**: 이메일, 비밀번호, 닉네임, 역할로 회원 가입
- **검증**: 이메일 중복 체크
- **응답**: 회원 정보 (memberId, email, nickname, role, createdAt)

### 2. 로그인 API
- **엔드포인트**: `POST /api/members/login`
- **기능**: 이메일/비밀번호로 로그인하여 JWT 토큰 발급
- **응답**: Access Token, Refresh Token, memberId, nickname

### 3. 로그아웃 API
- **엔드포인트**: `POST /api/members/logout`
- **인증**: Bearer Token 필요
- **기능**: 로그아웃 처리 (현재는 stateless JWT이므로 단순 성공 응답)

### 4. 토큰 재발급 API
- **엔드포인트**: `POST /api/members/token/refresh`
- **기능**: Refresh Token으로 새로운 Access Token과 Refresh Token 발급
- **검증**: Refresh Token만 허용 (Access Token 사용 시 에러)

### 5. 구글 로그인 API
- **엔드포인트**: `POST /api/members/oauth/google`
- **기능**: Google ID Token으로 로그인하여 JWT 토큰 발급
- **검증**: Google ID Token 검증 (JWT 파싱, 클레임 검증, 서명 검증)
- **응답**: Access Token, Refresh Token, isNewMember, memberId, nickname
- **에러**: 401 Unauthorized, `GOOGLE_AUTH_INVALID` (유효하지 않은 Google ID Token)

### 6. 소셜 인증 검증 API
- **엔드포인트**: `POST /api/members/oauth/verify`
- **기능**: 소셜 인증 토큰 검증 후 JWT 토큰 발급 (기존 회원만)
- **검증**: 소셜 토큰 검증 (현재 Google만 지원)
- **응답**: Access Token, Refresh Token, memberId
- **에러**: 401 Unauthorized, `OAUTH_TOKEN_INVALID` (유효하지 않은 소셜 토큰 또는 존재하지 않는 회원)

### 7. 소셜 계정 연결 API
- **엔드포인트**: `POST /api/members/me/oauth/link`
- **인증**: Bearer Token 필요
- **기능**: 현재 로그인한 회원에 소셜 계정 연결
- **검증**: 소셜 토큰 검증, 이미 연결된 계정인지 확인
- **응답**: linkedProvider
- **에러**: 
  - 401 Unauthorized, `OAUTH_TOKEN_INVALID` (유효하지 않은 소셜 토큰)
  - 409 Conflict, `OAUTH_ALREADY_LINKED` (이미 연결된 소셜 계정)

## 수정/생성된 파일

### 회원 도메인 (domain/member)

#### Controller
- `src/main/kotlin/com/bandchu/api/domain/member/controller/MemberController.kt` (신규)
  - 회원 가입, 로그인, 로그아웃, 토큰 재발급 엔드포인트 구현
  - 구글 로그인, 소셜 인증 검증, 소셜 계정 연결 엔드포인트 구현

#### Service
- `src/main/kotlin/com/bandchu/api/domain/member/service/MemberService.kt` (신규)
  - 회원 가입, 로그인, 로그아웃, 토큰 재발급 비즈니스 로직 구현
  - 구글 로그인, 소셜 인증 검증, 소셜 계정 연결 비즈니스 로직 구현

- `src/main/kotlin/com/bandchu/api/domain/member/service/GoogleOAuthService.kt` (신규)
  - Google ID Token 검증 서비스
  - JWT 파싱 및 구조 검증
  - 클레임 검증 (iss, aud, exp)
  - 서명 검증 (Google 공개키 사용)

- `src/main/kotlin/com/bandchu/api/domain/member/service/GoogleOAuthResult.kt` (신규)
  - 구글 로그인 결과 데이터 클래스

- `src/main/kotlin/com/bandchu/api/domain/member/service/OAuthVerifyResult.kt` (신규)
  - 소셜 인증 검증 결과 데이터 클래스

- `src/main/kotlin/com/bandchu/api/domain/member/service/OAuthLinkResult.kt` (신규)
  - 소셜 계정 연결 결과 데이터 클래스

- `src/main/kotlin/com/bandchu/api/domain/member/service/LoginResult.kt` (신규)
  - 로그인 결과 데이터 클래스 (accessToken, refreshToken, memberId, nickname)

- `src/main/kotlin/com/bandchu/api/domain/member/service/TokenPair.kt` (신규)
  - 토큰 쌍 데이터 클래스 (accessToken, refreshToken)

#### Repository
- `src/main/kotlin/com/bandchu/api/domain/member/repository/MemberRepository.kt` (신규)
  - `save()`: 회원 저장
  - `existsByEmail()`: 이메일 중복 체크
  - `findByEmail()`: 이메일로 회원 조회
  - `findById()`: ID로 회원 조회
  - `findByGoogleId()`: Google ID로 회원 조회
  - `updateGoogleId()`: 회원의 Google ID 업데이트

#### DTO
- `src/main/kotlin/com/bandchu/api/domain/member/dto/SignupRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/SignupResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/LoginRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/LoginResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/RefreshTokenRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/RefreshTokenResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/GoogleOAuthRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/GoogleOAuthResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/OAuthVerifyRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/OAuthVerifyResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/OAuthLinkRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/OAuthLinkResponse.kt` (신규)

#### Model
- `src/main/kotlin/com/bandchu/api/domain/member/model/Member.kt` (수정)
  - 필드 추가: id, email, password, nickname, role, googleId, createdAt

#### Table
- `src/main/kotlin/com/bandchu/api/domain/member/table/MemberTable.kt` (수정)
  - 컬럼 추가: email, password, nickname, role, google_id, createdAt

#### Test
- `src/test/kotlin/com/bandchu/api/domain/member/controller/MemberControllerTest.kt` (신규)
  - 회원 가입, 로그인, 로그아웃, 토큰 재발급 테스트 코드 작성

### 글로벌 (global)

#### Security
- `src/main/kotlin/com/bandchu/api/global/security/JwtService.kt` (신규)
  - JWT 토큰 생성, 검증, 파싱 기능
  - Access Token, Refresh Token 생성
  - 토큰에서 memberId, role, tokenType 추출

- `src/main/kotlin/com/bandchu/api/global/security/JwtAuthenticationFilter.kt` (신규)
  - JWT 토큰 인증 필터
  - Authorization 헤더에서 Bearer Token 추출 및 검증
  - Access Token만 허용 (Refresh Token 사용 시 에러)
  - SecurityContext에 인증 정보 설정

#### Config
- `src/main/kotlin/com/bandchu/api/global/config/SecurityConfig.kt` (신규)
  - Spring Security 설정
  - JWT 필터 등록
  - 공개 엔드포인트: `/api/members/signup`, `/api/members/login`, `/api/members/token/refresh`, `/api/members/oauth/google`, `/api/members/oauth/verify`
  - 인증 필요 엔드포인트: `/api/members/logout`, `/api/members/me/**`

- `src/main/kotlin/com/bandchu/api/global/config/DatabaseConfig.kt` (수정)
  - `MemberTable` 스키마 생성 추가

#### Exception
- `src/main/kotlin/com/bandchu/api/global/exception/ErrorCode.kt` (수정)
  - `USER_EMAIL_DUPLICATED`: 이메일 중복 에러
  - `USER_INVALID_CREDENTIAL`: 잘못된 인증 정보 에러
  - `INVALID_TOKEN`: 유효하지 않은 토큰 에러
  - `INVALID_REFRESH_TOKEN`: 유효하지 않은 리프레시 토큰 에러
  - `GOOGLE_AUTH_INVALID`: 구글 인증이 유효하지 않음 에러
  - `OAUTH_TOKEN_INVALID`: 소셜 인증 토큰이 유효하지 않음 에러
  - `OAUTH_ALREADY_LINKED`: 이미 연결된 소셜 계정 에러

### 기타
- `build.gradle.kts` (수정)
  - JWT 의존성 추가:
    - `io.jsonwebtoken:jjwt-api:0.12.3`
    - `io.jsonwebtoken:jjwt-impl:0.12.3`
    - `io.jsonwebtoken:jjwt-jackson:0.12.3`

- `src/main/resources/application.yml` (수정)
  - Google OAuth 설정 추가:
    - `oauth.google.client-id`: Google 클라이언트 ID
    - `spring.security.oauth2.client.provider.google.jwk-set-uri`: Google JWK Set URI

## curl 테스트 결과

### 1. 회원 가입 API
```bash
POST /api/members/signup
```
- ✅ **성공 케이스**: 201 Created, 회원 정보 반환
- ✅ **실패 케이스**: 409 Conflict, `USER_EMAIL_DUPLICATED` 에러

### 2. 로그인 API
```bash
POST /api/members/login
```
- ✅ **성공 케이스**: 200 OK, Access Token, Refresh Token, memberId, nickname 반환
- ✅ **실패 케이스 (잘못된 비밀번호)**: 401 Unauthorized, `USER_INVALID_CREDENTIAL` 에러
- ✅ **실패 케이스 (존재하지 않는 이메일)**: 401 Unauthorized, `USER_INVALID_CREDENTIAL` 에러

### 3. 로그아웃 API
```bash
POST /api/members/logout
Authorization: Bearer {accessToken}
```
- ✅ **성공 케이스**: 200 OK, "로그아웃되었습니다." 메시지
- ✅ **실패 케이스 (토큰 없음)**: 403 Forbidden (Spring Security 기본 동작)
- ✅ **실패 케이스 (유효하지 않은 토큰)**: 401 Unauthorized, `INVALID_TOKEN` 에러
- ✅ **실패 케이스 (Refresh Token 사용)**: 401 Unauthorized, `INVALID_TOKEN` 에러 (Access Token만 허용)

### 4. 토큰 재발급 API
```bash
POST /api/members/token/refresh
Content-Type: application/json
{
  "refreshToken": "string"
}
```
- ✅ **성공 케이스**: 200 OK, 새로운 Access Token과 Refresh Token 반환
- ✅ **실패 케이스 (유효하지 않은 토큰)**: 401 Unauthorized, `INVALID_REFRESH_TOKEN` 에러
- ✅ **실패 케이스 (Access Token 사용)**: 401 Unauthorized, `INVALID_REFRESH_TOKEN` 에러 (Refresh Token만 허용)

### 5. 구글 로그인 API
```bash
POST /api/members/oauth/google
Content-Type: application/json
{
  "idToken": "google-id-token"
}
```
- ✅ **성공 케이스 (신규 회원)**: 200 OK, Access Token, Refresh Token, isNewMember=true, memberId, nickname 반환
- ✅ **성공 케이스 (기존 회원)**: 200 OK, Access Token, Refresh Token, isNewMember=false, memberId, nickname 반환
- ✅ **실패 케이스 (유효하지 않은 ID Token)**: 401 Unauthorized, `GOOGLE_AUTH_INVALID` 에러

### 6. 소셜 인증 검증 API
```bash
POST /api/members/oauth/verify
Content-Type: application/json
{
  "provider": "GOOGLE",
  "token": "oauth-token"
}
```
- ✅ **성공 케이스**: 200 OK, Access Token, Refresh Token, memberId 반환
- ✅ **실패 케이스 (유효하지 않은 토큰)**: 401 Unauthorized, `OAUTH_TOKEN_INVALID` 에러
- ✅ **실패 케이스 (존재하지 않는 회원)**: 401 Unauthorized, `OAUTH_TOKEN_INVALID` 에러

### 7. 소셜 계정 연결 API
```bash
POST /api/members/me/oauth/link
Authorization: Bearer {accessToken}
Content-Type: application/json
{
  "provider": "GOOGLE",
  "token": "google-oauth-token"
}
```
- ✅ **성공 케이스**: 200 OK, linkedProvider 반환
- ✅ **실패 케이스 (유효하지 않은 토큰)**: 401 Unauthorized, `OAUTH_TOKEN_INVALID` 에러
- ✅ **실패 케이스 (이미 연결된 계정)**: 409 Conflict, `OAUTH_ALREADY_LINKED` 에러

## 테스트 상태

### 단위 테스트
- ✅ 테스트 코드 작성 완료 (`MemberControllerTest.kt`)
  - 회원 가입, 로그인, 로그아웃, 토큰 재발급 테스트
  - 구글 로그인, 소셜 인증 검증, 소셜 계정 연결 테스트
- ❌ **아직 테스트 실행은 하지 않음**
- ⚠️ chat 도메인 테스트 파일(`ChatMessageServiceTest.kt`)에 컴파일 에러가 있어 전체 테스트 실행 시 실패함.

### 통합 테스트 (curl)
- ✅ 모든 API curl 테스트 완료
- ✅ 정상 동작 확인

## 주의!!! 데이터베이스 변경사항

**버전 2.0에서 `members` 테이블 구조가 변경되었습니다.**

### 변경 내용
- `google_id` 컬럼 추가 (VARCHAR(255), NULL 허용)
- `members_google_id_unique_idx` UNIQUE 인덱스 추가 (NULL 값은 중복 허용)

### 주의사항
- 기존 데이터베이스를 사용하는 경우, **반드시 마이그레이션을 실행**해야 합니다.
- 마이그레이션을 실행하지 않으면 Google OAuth 관련 API가 동작하지 않습니다.
- 마이그레이션 SQL:
  ```sql
  ALTER TABLE members ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);
  CREATE UNIQUE INDEX IF NOT EXISTS members_google_id_unique_idx 
  ON members (google_id) WHERE google_id IS NOT NULL;
  ```

## TODO

1. **TDD 테스트 실행**
   - `MemberControllerTest.kt` 실행하여 테스트 통과 확인

2. **비밀번호 암호화**
   - 현재 평문 저장 중 (TODO 주석 있음)
   - BCrypt 등으로 암호화 필요

3. **유효성 검증 에러 처리**
   - 빈 값 등 유효성 검증 실패 시 500 에러 대신 400 Bad Request 반환하도록 개선

## 보안 고려사항

1. **JWT Secret Key**
   - 현재 기본값 사용 중 (`bandchu-secret-key-for-jwt-token-generation-minimum-256-bits`)
   - 운영 환경에서는 환경 변수로 관리 필요

2. **토큰 만료 시간**
   - Access Token: 1시간 (기본값)
   - Refresh Token: 7일 (기본값)
   - 환경 변수로 설정 가능

3. **비밀번호 보안**
   - 현재 평문 저장 중
   - 암호화 필수

---

**작성일**: 2025-11-26  
**최종 수정일**: 2025-11-26  
**작성자**: 신진수  
**버전**: 2.0

## 변경 이력

### 버전 2.0 (2025-11-26)
- 구글 로그인 API 추가 (`POST /api/members/oauth/google`)
- 소셜 인증 검증 API 추가 (`POST /api/members/oauth/verify`)
- 소셜 계정 연결 API 추가 (`POST /api/members/me/oauth/link`)
- Google ID Token 검증 기능 구현 (JWT 파싱, 클레임 검증, 서명 검증)
- Member 모델에 `googleId` 필드 추가
- MemberTable에 `google_id` 컬럼 추가
- Spring Security OAuth2 플로우 사용 X, 수동 ID Token 검증 방식 사용

