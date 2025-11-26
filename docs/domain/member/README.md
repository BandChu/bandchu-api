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

## 수정/생성된 파일

### 회원 도메인 (domain/member)

#### Controller
- `src/main/kotlin/com/bandchu/api/domain/member/controller/MemberController.kt` (신규)
  - 회원 가입, 로그인, 로그아웃, 토큰 재발급 엔드포인트 구현

#### Service
- `src/main/kotlin/com/bandchu/api/domain/member/service/MemberService.kt` (신규)
  - 회원 가입, 로그인, 로그아웃, 토큰 재발급 비즈니스 로직 구현

#### Repository
- `src/main/kotlin/com/bandchu/api/domain/member/repository/MemberRepository.kt` (신규)
  - `save()`: 회원 저장
  - `existsByEmail()`: 이메일 중복 체크
  - `findByEmail()`: 이메일로 회원 조회
  - `findById()`: ID로 회원 조회

#### DTO
- `src/main/kotlin/com/bandchu/api/domain/member/dto/SignupRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/SignupResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/LoginRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/LoginResponse.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/RefreshTokenRequest.kt` (신규)
- `src/main/kotlin/com/bandchu/api/domain/member/dto/RefreshTokenResponse.kt` (신규)

#### Model
- `src/main/kotlin/com/bandchu/api/domain/member/model/Member.kt` (수정)
  - 필드 추가: id, email, password, nickname, role, createdAt

#### Table
- `src/main/kotlin/com/bandchu/api/domain/member/table/MemberTable.kt` (수정)
  - 컬럼 추가: email, password, nickname, role, createdAt

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
  - 공개 엔드포인트: `/api/members/signup`, `/api/members/login`, `/api/members/token/refresh`
  - 인증 필요 엔드포인트: `/api/members/logout`

- `src/main/kotlin/com/bandchu/api/global/config/DatabaseConfig.kt` (수정)
  - `MemberTable` 스키마 생성 추가

#### Exception
- `src/main/kotlin/com/bandchu/api/global/exception/ErrorCode.kt` (수정)
  - `USER_EMAIL_DUPLICATED`: 이메일 중복 에러
  - `USER_INVALID_CREDENTIAL`: 잘못된 인증 정보 에러
  - `INVALID_TOKEN`: 유효하지 않은 토큰 에러
  - `INVALID_REFRESH_TOKEN`: 유효하지 않은 리프레시 토큰 에러

### 기타
- `build.gradle.kts` (수정)
  - JWT 의존성 추가:
    - `io.jsonwebtoken:jjwt-api:0.12.3`
    - `io.jsonwebtoken:jjwt-impl:0.12.3`
    - `io.jsonwebtoken:jjwt-jackson:0.12.3`

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

## 테스트 상태

### 단위 테스트
- ✅ 테스트 코드 작성 완료 (`MemberControllerTest.kt`)
- ❌ **아직 테스트 실행은 하지 않음**
- ⚠️ chat 도메인 테스트 파일(`ChatMessageServiceTest.kt`)에 컴파일 에러가 있어 전체 테스트 실행 시 실패함.

### 통합 테스트 (curl)
- ✅ 모든 API curl 테스트 완료
- ✅ 정상 동작 확인

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
**작성자**: 신진수  
**버전**: 1.0

