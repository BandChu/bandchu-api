# API 명세서

## 목차
- [공통 응답 포맷](#공통-응답-포맷)
- [에러 응답 포맷 (ProblemDetail)](#에러-응답-포맷-problemdetail)
- [회원 관리 API](#회원-관리-api)
    - [회원 가입](#회원-가입)
    - [로그인](#로그인)
    - [로그아웃](#로그아웃)
    - [토큰 재발급](#토큰-재발급)
    - [구글 로그인](#구글-로그인)
    - [소셜 인증 검증](#소셜-인증-검증)
    - [소셜 계정 연결](#소셜-계정-연결)
    - [회원 탈퇴](#회원-탈퇴)
    - [프로필 초기 설정](#프로필-초기-설정)
    - [내 정보 조회](#내-정보-조회)
    - [역할 업데이트](#역할-업데이트)
- [구독 API](#구독-api)
    - [아티스트 구독](#아티스트-구독)
    - [구독 취소](#구독-취소)
    - [구독 목록 조회](#구독-목록-조회)
- [아티스트 API](#아티스트-api)
    - [전체 아티스트 목록 조회](#전체-아티스트-목록-조회)
    - [아티스트 및 공연 검색](#아티스트-및-공연-검색)
    - [아티프로필 상세 조회](#아티프로필-상세-조회)
    - [아티프로필 수정](#아티프로필-수정)
- [앨범 API](#앨범-api)
  - [앨범 등록](#앨범-등록)
  - [앨범 상세 조회](#앨범-상세-조회)
  - [앨범 삭제](#앨범-삭제)
- [공연 API](#공연-api)
  - [공연 등록](#공연-등록)
  - [구독한 아티스트의 공연 정보 조회](#구독한-아티스트의-공연-정보-조회)
  - [공연 상세 조회](#공연-상세-조회)
  - [공연 수정](#공연-수정)
  - [공연 삭제](#공연-삭제)
- [채팅 API](#채팅-api)
    - [채팅방 생성](#채팅방-생성)
    - [메시지 보내기](#메시지-보내기)
    - [채팅방 목록 조회](#채팅방-목록-조회)
    - [메시지 목록 조회](#메시지-목록-조회)
    - [메시지 읽음 처리](#메시지-읽음-처리)

<br>

---

## 공통 응답 포맷

성공적인 API 응답은 다음 형식을 따릅니다:

```json
{
  "success": true,
  "data": {
    /* 응답 데이터 */
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

<br>

---

## 에러 응답 포맷 (ProblemDetail)

에러 발생 시 RFC 9457 표준을 따르는 ProblemDetail 포맷으로 응답합니다:

```json
{
  "type": "https://api.bandchu.com/errors/{error_code}",
  "title": "{HTTP Reason Phrase}",
  "status": 400,
  "detail": "{상황별 상세 설명}",
  "instance": "/api/...",
  "code": "{ERROR_CODE}",
  "timestamp": "{ISO-8601 UTC}"
}
```

### 공통 에러 타입

| 타입 | 설명 | HTTP 상태 코드 |
|------|------|--------------|
| Bad Request | 잘못된 요청 | 400 |
| Unauthorized | 인증되지 않은 요청 | 401 |
| Forbidden | 권한이 없는 요청 | 403 |
| Not Found | 요청한 리소스를 찾을 수 없음 | 404 |
| Conflict | 리소스 충돌 발생 | 409 |
| Internal Server Error | 서버 내부 오류 | 500 |

### 예시

```json
{
  "type": "https://api.bandchu.com/errors/user-email-duplicated",
  "title": "Conflict",
  "status": 409,
  "detail": "이미 사용 중인 이메일입니다.",
  "instance": "/api/users/signup",
  "code": "USER_EMAIL_DUPLICATED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

- title은 공통 에러 타입의 HTTP Reason Phrase 기준
- 도메인별 상세 오류 구분은 code 필드에서 관리

<br>

---

## 회원 관리 API

회원 관련 작업을 수행하는 API입니다.

### 회원 가입

`POST /api/members/signup`

새로운 회원을 생성합니다.

#### 요청 헤더
```
Content-Type: application/json
```

#### 요청 본문
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "사용자",
  "role": "FAN"
}
```

#### 응답 본문 (성공 - 201 Created)
```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "nickname": "사용자",
    "role": "FAN",
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "createdAt": "2025-11-28T00:00:00Z"
  },
  "message": "회원 가입이 완료되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/user-email-duplicated",
  "title": "Conflict",
  "status": 409,
  "detail": "이미 사용 중인 이메일입니다.",
  "instance": "/api/members/signup",
  "code": "USER_EMAIL_DUPLICATED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 로그인

`POST /api/members/login`

이메일/비밀번호로 로그인하여 JWT 토큰을 발급합니다.

#### 요청 헤더
```
Content-Type: application/json
```

#### 요청 본문
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "memberId": 1,
    "nickname": "사용자"
  },
  "message": "로그인되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/user-invalid-credential",
  "title": "Unauthorized",
  "status": 401,
  "detail": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "instance": "/api/members/login",
  "code": "USER_INVALID_CREDENTIAL",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 로그아웃

`POST /api/members/logout`

로그아웃을 처리합니다. (현재는 stateless JWT이므로 단순 성공 응답)

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": null,
  "message": "로그아웃되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/invalid-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "유효하지 않은 토큰입니다.",
  "instance": "/api/members/logout",
  "code": "INVALID_TOKEN",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 토큰 재발급

`POST /api/members/token/refresh`

Refresh Token으로 새로운 Access Token과 Refresh Token을 발급합니다.

#### 요청 헤더
```
Content-Type: application/json
```

#### 요청 본문
```json
{
  "refreshToken": "eyJhbGci..."
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  },
  "message": "토큰이 재발급되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/invalid-refresh-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "유효하지 않은 리프레시 토큰입니다.",
  "instance": "/api/members/token/refresh",
  "code": "INVALID_REFRESH_TOKEN",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 구글 로그인

`POST /api/members/oauth/google`

Google ID Token으로 로그인하여 JWT 토큰을 발급합니다.

#### 요청 헤더
```
Content-Type: application/json
```

#### 요청 본문
```json
{
  "idToken": "eyJhbGci..."
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "isNewMember": true,
    "memberId": 1,
    "nickname": "사용자"
  },
  "message": "구글 로그인이 완료되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/google-auth-invalid",
  "title": "Unauthorized",
  "status": 401,
  "detail": "구글 인증이 유효하지 않습니다.",
  "instance": "/api/members/oauth/google",
  "code": "GOOGLE_AUTH_INVALID",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 소셜 인증 검증

`POST /api/members/oauth/verify`

소셜 인증 토큰을 검증한 후 JWT 토큰을 발급합니다. (기존 회원만)

#### 요청 헤더
```
Content-Type: application/json
```

#### 요청 본문
```json
{
  "provider": "GOOGLE",
  "token": "oauth-token"
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "memberId": 1
  },
  "message": "소셜 인증이 완료되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/oauth-token-invalid",
  "title": "Unauthorized",
  "status": 401,
  "detail": "소셜 인증 토큰이 유효하지 않습니다.",
  "instance": "/api/members/oauth/verify",
  "code": "OAUTH_TOKEN_INVALID",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 소셜 계정 연결

`POST /api/members/me/oauth/link`

현재 로그인한 회원에 소셜 계정을 연결합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "provider": "GOOGLE",
  "token": "google-oauth-token"
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "linkedProvider": "GOOGLE"
  },
  "message": "소셜 계정이 연결되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/oauth-already-linked",
  "title": "Conflict",
  "status": 409,
  "detail": "이미 연결된 소셜 계정입니다.",
  "instance": "/api/members/me/oauth/link",
  "code": "OAUTH_ALREADY_LINKED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 회원 탈퇴

`DELETE /api/members/me`

현재 로그인한 회원을 탈퇴합니다. (Idempotent: 이미 삭제된 회원에 대한 재요청도 성공으로 처리)

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": null,
  "message": "회원 탈퇴가 완료되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/invalid-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "유효하지 않은 토큰입니다.",
  "instance": "/api/members/me",
  "code": "INVALID_TOKEN",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 프로필 초기 설정

`PATCH /api/members/me/profile/setup`

회원가입 절차에 삽입할 목적으로 프로필을 초기 설정합니다. (닉네임, 프로필 이미지 URL)

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "nickname": "새사용자",
  "profileImageUrl": "https://example.com/profile.jpg"
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "nickname": "새사용자",
    "profileImageUrl": "https://example.com/profile.jpg"
  },
  "message": "프로필 초기 설정이 완료되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/invalid-nickname",
  "title": "Bad Request",
  "status": 400,
  "detail": "닉네임 형식이 올바르지 않습니다.",
  "instance": "/api/members/me/profile/setup",
  "code": "INVALID_NICKNAME",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 내 정보 조회

`GET /api/members/me`

현재 로그인한 사용자의 정보를 조회합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "nickname": "사용자",
    "role": "FAN",
    "profileImageUrl": "https://example.com/profile.jpg"
  },
  "message": "사용자 정보 조회에 성공했습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/invalid-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "유효하지 않은 토큰입니다.",
  "instance": "/api/members/me",
  "code": "INVALID_TOKEN",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 역할 업데이트

`PATCH /api/members/me/role`

사용자의 역할(FAN/ARTIST)을 변경합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "role": "ARTIST"
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "role": "ARTIST"
  },
  "message": "역할이 업데이트되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/invalid-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "유효하지 않은 토큰입니다.",
  "instance": "/api/members/me/role",
  "code": "INVALID_TOKEN",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

<br>

---

## 구독 API

구독 관련 작업을 수행하는 API입니다.

### 아티스트 구독

`POST /api/subscriptions`

팬(FAN)이 아티스트를 구독합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "artiProfileId": 12
}
```

#### 응답 본문 (성공 - 201 Created)
```json
{
  "success": true,
  "data": {
    "subscriptionId": 88,
    "memberId": 1,
    "artiProfileId": 12,
    "createdAt": "2025-11-28T00:00:00Z"
  },
  "message": "아티스트를 구독했습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/subscription-insufficient-role",
  "title": "Forbidden",
  "status": 403,
  "detail": "팬만 구독할 수 있습니다.",
  "instance": "/api/subscriptions",
  "code": "SUBSCRIPTION_INSUFFICIENT_ROLE",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

```json
{
  "type": "https://api.bandchu.com/errors/subscription-duplicated",
  "title": "Conflict",
  "status": 409,
  "detail": "이미 구독 중인 아티스트입니다.",
  "instance": "/api/subscriptions",
  "code": "SUBSCRIPTION_DUPLICATED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 구독 취소

`DELETE /api/subscriptions/{artiProfileId}`

팬(FAN)이 구독 중인 아티스트의 구독을 취소합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 경로 파라미터

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| artiProfileId | Long | 구독 취소할 아티스트 ID |

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": null,
  "message": "구독이 취소되었습니다."
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/subscription-not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "구독 중이 아닌 아티스트입니다.",
  "instance": "/api/subscriptions/12",
  "code": "SUBSCRIPTION_NOT_FOUND",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 구독 목록 조회

`GET /api/subscriptions`

현재 로그인한 사용자가 구독한 아티스트 목록을 조회합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": [
    {
      "artiProfileId": 1,
      "artistName": "NewJeans",
      "profileImage": "https://example.com/profile.jpg"
    }
  ],
  "message": "구독 목록 조회 성공"
}
```

#### 에러 응답

```json
{
  "type": "https://api.bandchu.com/errors/invalid-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "유효하지 않은 토큰입니다.",
  "instance": "/api/subscriptions",
  "code": "INVALID_TOKEN",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

<br>

---

<br>

---

## 아티스트 API

아티스트 관련 작업을 수행하는 API입니다. 

> 실제 도메인∙DB 레벨에서는 아티스트 관련 정보를 **아티프로필(ArtiProfile)** 이라는 네이밍으로 관리합니다.

### 전체 아티스트 목록 조회

`GET /api/artists`

등록된 모든 아티스트 목록을 조회합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "artists": [
      {
        "artistId": 1,
        "name": "아티스트 이름",
        "profileImageUrl": "...",
        "createdAt": "2025-11-20T03:21:00Z"
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 아티스트 및 공연 검색

`GET /api/artists/search`

검색 키워드가 포함된 아티스트, 해당 아티스트의 공연, 검색 키워드가 포함된 공연 목록을 조회합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 쿼리 파라미터

| 이름      | 타입     | 필수 | 설명     |
|---------|--------|----|--------|
| keyword | string | Y  | 검색 키워드 |

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "artists": [
      {
        "artistId": 1,
        "name": "검색 키워드가 포함된 아티스트 이름",
        "profileImageUrl": "..."
      }
    ],
    "concerts": [
      {
        "concertId": 11,
        "title": "검색 키워드가 포함된 아티스트의 공연 제목",
        "place": "공연 장소",
        "posterImageUrl": "..."
      },
      {
        "concertId": 10,
        "title": "검색 키워드가 포함된 공연 제목",
        "place": "공연 장소",
        "posterImageUrl": "..."
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 아티프로필 상세 조회

`GET /api/artists/{artistId}`

특정 아티스트의 프로필 상세 정보를 조회합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "artistId": 1,
    "name": "아티스트 이름",
    "profileImageUrl": "...",
    "description": "아티스트 소개글",
    "genre": "아티스트의 음악 장르",
    "sns": [
      {
        "platform": "youtube",
        "url": "..."
      },
      {
        "platform": "soundcloud",
        "url": "..."
      },
      {
        "platform": "instagram",
        "url": "..."
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 아티프로필 수정

`PATCH /api/artists/{artistId}`

아티스트가 프로필 상세 정보를 수정합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "name": "아티스트 이름",
  "profileImageUrl": "...",
  "description": "업데이트된 아티스트 소개글",
  "genre": "아티스트의 음악 장르",
  "sns": [
    {
      "platform": "youtube",
      "url": "업데이트된 ..."
    },
    {
      "platform": "soundcloud",
      "url": "..."
    },
    {
      "platform": "instagram",
      "url": "..."
    }
  ]
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "artistId": 1,
    "name": "아티스트 이름",
    "profileImageUrl": "...",
    "description": "업데이트된 아티스트 소개글",
    "genre": "아티스트의 음악 장르",
    "sns": [
      {
        "platform": "youtube",
        "url": "업데이트된 ..."
      },
      {
        "platform": "soundcloud",
        "url": "..."
      },
      {
        "platform": "instagram",
        "url": "..."
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 에러 응답 (권한 없음 - 403 Forbidden)

```json
{
  "type": "https://api.bandchu.com/errors/artist-access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "해당 아티프로필에 대한 접근 권한이 없습니다.",
  "instance": "/api/artists/{artistId}",
  "code": "ARTIST_ACCESS_DENIED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

<br>

---

## 앨범 API

앨범 관련 작업을 수행하는 API입니다.

### 앨범 등록

`POST /api/albums`

아티스트가 새로운 앨범을 등록합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "name": "앨범명",
  "coverImageUrl": "...",
  "releaseDate": "2025-11-20T03:21:00.123Z",
  "description": "앨범 소개",
  "tracks": [
    {
      "name": "트랙명 1"
    },
    {
      "name": "트랙명 2"
    }
  ]
}
```

#### 응답 본문 (성공 - 200 Ok)

```json
{
  "success": true,
  "data": {
    "albumId": 1,
    "name": "앨범명",
    "coverImageUrl": "...",
    "releaseDate": "2025-11-20T03:21:00.123Z",
    "description": "앨범 소개",
    "tracks": [
      {
        "trackId": 1,
        "name": "트랙명 1"
      },
      {
        "trackId": 2,
        "name": "트랙명 2"
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 에러 응답 (권한 없음 - 404 Forbidden)

접속한 사용자의 역할이 `ARTIST`가 아닌 경우

```json
{
  "type": "https://api.bandchu.com/errors/album-access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "아티스트만 앨범을 등록할 수 있습니다.",
  "instance": "/api/albums",
  "code": "ALBUM_ACCESS_DENIED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 앨범 상세 조회

`GET /api/albums/{albumId}`

특정 앨범의 상세 정보를 조회합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "albumId": 1,
    "name": "앨범명",
    "coverImageUrl": "...",
    "releaseDate": "2025-11-20T03:21:00.123Z",
    "description": "앨범 소개",
    "tracks": [
      {
        "trackId": 1,
        "name": "트랙명 1"
      },
      {
        "trackId": 2,
        "name": "트랙명 2"
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 앨범 삭제

`DELETE /api/albums/{albumId}`

아티스트가 특정 앨범을 삭제합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": null,
  "message": "삭제가 완료되었습니다."
}
```

#### 에러 응답 (권한 없음 - 403 Forbidden)

```json
{
  "type": "https://api.bandchu.com/errors/album-access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "해당 앨범에 대한 접근 권한이 없습니다.",
  "instance": "/api/albums/{albumId}",
  "code": "ALBUM_ACCESS_DENIED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

<br>

---

## 공연 API

공연 관련 작업을 수행하는 API입니다.
UI상 달력 탭을 위한 데이터를 내려줍니다.

### 공연 등록

`POST /api/concerts`

역할이 아티스트인 회원이 새로운 공연을 등록합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "artistId": 1,
  "title": "공연 제목",
  "place": "공연 장소",
  "posterImageUrl": "...",
  "information": "공연 정보",
  "bookingSchedule": "2025-11-25T10:00:00Z",
  "bookingUrl": "...",
  "performingSchedule": [
    {
      "date": "2025-12-25T10:00:00Z"
    }
  ]
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "concertId": 1,
    "title": "공연 제목",
    "place": "공연 장소",
    "posterImageUrl": "...",
    "information": "공연 정보",
    "bookingSchedule": "2025-11-25T10:00:00Z",
    "bookingUrl": "...",
    "performingSchedule": [
      {
        "date": "2025-12-25T10:00:00Z"
      }
    ],
    "createdAt": "2025-11-25T10:00:00Z"
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 에러 응답 (권한 없음 - 403 Forbidden)

접속한 사용자의 역할이 `ARTIST`가 아닌 경우

```json
{
  "type": "https://api.bandchu.com/errors/concert-access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "아티스트만 공연을 등록할 수 있습니다.",
  "instance": "/api/concerts",
  "code": "CONCERT_ACCESS_DENIED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 구독한 아티스트의 공연 정보 조회

`GET /api/concerts/subscribed`

특정 회원이 구독한 아티스트 목록과 아티스트별 공연 일정, 공연 예매 일정을 조회합니다.  
아티스트 정보는 필터링에 사용됩니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "artists": [
      {
        "artistId": 1,
        "name": "아티스트 이름",
        "profileImageUrl": "...",
        "subscribedAt": "2025-11-20T03:21:00Z",
        "concerts": [
          {
            "concertId": 10,
            "title": "공연 제목",
            "place": "공연 장소",
            "performingSchedule": [
              {
                "date": "2025-12-25T10:00:00Z"
              }
            ],
            "bookingSchedule": "2025-11-25T10:00:00Z"
          }
        ]
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 공연 상세 조회

`GET /api/concerts/{concertId}`

특정 공연의 상세 정보를 조회합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "concertId": 1,
    "title": "공연 제목",
    "place": "공연 장소",
    "posterImageUrl": "...",
    "information": "공연 정보",
    "bookingSchedule": "2025-11-25T10:00:00Z",
    "bookingUrl": "...",
    "performingSchedule": [
      {
        "date": "2025-12-25T10:00:00Z"
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 공연 수정

`PATCH /api/concerts/{concertId}`

아티스트가 공연 상세 정보를 수정합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 요청 본문
```json
{
  "title": "수정된 공연 제목",
  "place": "수정된 공연 장소",
  "posterImageUrl": "...",
  "information": "수정된 공연 정보",
  "bookingSchedule": "2025-11-25T10:00:00Z",
  "bookingUrl": "...",
  "performingSchedule": [
    {
      "date": "2025-12-25T10:00:00Z"
    }
  ],
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
    "concertId": 1,
    "title": "수정된 공연 제목",
    "place": "수정된 공연 장소",
    "posterImageUrl": "...",
    "information": "수정된 공연 정보",
    "bookingSchedule": "2025-11-25T10:00:00Z",
    "bookingUrl": "...",
    "performingSchedule": [
      {
        "date": "2025-12-25T10:00:00Z"
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 에러 응답 (권한 없음 - 403 Forbidden)

```json
{
  "type": "https://api.bandchu.com/errors/concert-access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "해당 공연에 대한 접근 권한이 없습니다.",
  "instance": "/api/concerts/{concertId}",
  "code": "CONCERT_ACCESS_DENIED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

### 공연 삭제

`DELETE /api/concerts/{concertId}`

아티스트가 특정 공연을 삭제합니다.

#### 요청 헤더
```
Authorization: Bearer {token}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": null,
  "message": "삭제가 완료되었습니다."
}
```

#### 에러 응답 (권한 없음 - 403 Forbidden)

```json
{
  "type": "https://api.bandchu.com/errors/concert-access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "해당 공연에 대한 접근 권한이 없습니다.",
  "instance": "/api/concerts/{concertId}",
  "code": "CONCERT_ACCESS_DENIED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

<br>

---

## 채팅 API

채팅 관련 작업을 수행하는 API입니다.

### 채팅방 생성

` POST /api/chatrooms `

1:1 또는 그룹 채팅방을 생성합니다.

#### 요청 헤더
` Authorization: Bearer {token} `

#### 요청 본문
```
{
  "roomType": "DIRECT", 
  "name": "알고리즘 스터디", 
  "memberIds": [2, 3] 
}
```

#### 응답 (성공 - 201 Created)
```json
{
  "success": true,
  "data": {
    "roomId": 10,
    "roomType": "GROUP",
    "name": "알고리즘 스터디",
    "createdAt": "2025-11-21T12:00:00Z"
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 에러 응답 예시
```json
{
  "type": "https://api.bandchu.com/errors/chatroom-duplicate-direct",
  "title": "Conflict",
  "status": 409,
  "detail": "이미 두 사용자 간의 1:1 채팅방이 존재합니다.",
  "instance": "/api/chatrooms",
  "code": "CHATROOM_DUPLICATE_DIRECT",
  "timestamp": "2025-11-21T03:21:00.123Z"
}
```

### 메시지 보내기

` POST /api/chatrooms/{roomId}/messages `

텍스트 또는 이미지 메시지를 전송합니다.

#### 요청 헤더
` Authorization: Bearer {token} `

#### 요청 본문 (TEXT)
```json
{
  "messageType": "TEXT",
  "content": "안녕하세요!"
} 
```

#### 요청 본문 (IMAGE)
```json
{
  "messageType": "IMAGE",
  "fileUrl": "https://s3.ap-northeast-2.amazonaws.com/xxx/yyy.png"
}
```

#### 응답 (성공 - 201 Created)
```json
{
  "success": true,
  "data": {
    "messageId": 112,
    "roomId": 10,
    "senderId": 1,
    "messageType": "TEXT",
    "content": "안녕하세요!",
    "fileUrl": null,
    "createdAt": "2025-11-21T12:10:00Z"
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 에러 응답 예시
```json
{
  "type": "https://api.bandchu.com/errors/chatroom-not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "해당 채팅방을 찾을 수 없습니다.",
  "instance": "/api/chatrooms/10/messages",
  "code": "CHATROOM_NOT_FOUND",
  "timestamp": "2025-11-21T03:21:00.123Z"
}
```

### 채팅방 목록 조회
` GET /api/chatrooms `

사용자가 속한 모든 채팅방을 조회합니다.

#### 요청 헤더
` Authorization: Bearer {token} `

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "data": {
    "rooms": [
      {
        "roomId": 10,
        "roomType": "GROUP",
        "name": "알고리즘 스터디",
        "lastMessage": "내일 회의는?",
        "unreadCount": 3,
        "updatedAt": "2025-11-21T12:10:00Z"
      }
    ]
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 메시지 목록 조회

` GET /api/chatrooms/{roomId}/messages?cursor={messageId}&size=30 `

특정 채팅방의 메시지를 페이징 형태로 조회합니다.

#### 요청 헤더
` Authorization: Bearer {token} `

#### 쿼리 파라미터
- cursor:	마지막으로 받은 메시지 ID (기본: 최신)
- size:	한번에 받을 메시지 수 (기본: 30)

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "messageId": 111,
        "senderId": 2,
        "messageType": "TEXT",
        "content": "사진 보내줘!",
        "fileUrl": null,
        "createdAt": "2025-11-21T12:05:00Z"
      },
      {
        "messageId": 112,
        "senderId": 1,
        "messageType": "IMAGE",
        "content": null,
        "fileUrl": "https://s3.../img.png",
        "createdAt": "2025-11-21T12:06:00Z"
      }
    ],
    "nextCursor": 110
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

### 메시지 읽음 처리

` PATCH /api/chatrooms/{roomId}/read `

사용자가 특정 메시지까지 읽었음을 기록합니다.

#### 요청 헤더
` Authorization: Bearer {token} `

#### 요청 본문
```
{
  "lastReadMessageId": 112
}
```

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "data": {
    "roomId": 10,
    "lastReadMessageId": 112
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

#### 에러 응답 예시
```json
{
  "type": "https://api.bandchu.com/errors/not-chatroom-member",
  "title": "Forbidden",
  "status": 403,
  "detail": "해당 채팅방의 참여자가 아닙니다.",
  "instance": "/api/chatrooms/10/read",
  "code": "NOT_CHATROOM_MEMBER",
  "timestamp": "2025-11-21T03:21:00.123Z"
}
```