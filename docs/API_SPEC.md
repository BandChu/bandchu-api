# API 명세서

## 목차
- [공통 응답 포맷](#공통-응답-포맷)
- [에러 응답 포맷 (ProblemDetail)](#에러-응답-포맷-problemdetail)
- [회원 관리 API](#회원-관리-api-예시)
    - [회원 가입](#회원-가입)

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
  "code": "{ERROR_CODE}",
  "timestamp": "{ISO-8601 UTC}",
  "path": "/api/..."
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
  "code": "USER_EMAIL_DUPLICATED",
  "timestamp": "2025-11-20T03:21:00.123Z",
  "path": "/api/users/signup"
}
```

- title은 공통 에러 타입의 HTTP Reason Phrase 기준
- 도메인별 상세 오류 구분은 code 필드에서 관리

<br>

---

## 회원 관리 API (예시)

### 회원 가입

`POST /api/posts`

새로운 회원을 생성합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token} (회원 가입에선 필요 없는데 다른 API 참고용으로 넣어둠!!)
```

#### 요청 파라미터
_(필요한 경우 작성, 없으면 제거)_


#### 요청 본문
```json
{
  "email": "example@domain.com",
  "password": "string",
  "name": "홍길동"
}
```

#### 응답 본문 (성공 - 200 Ok)
```json
{
  "success": true,
  "data": {
   /* 예시: 생성된 게시글 정보 */
  },
  "message": "게시글 작성이 완료되었습니다."
}
```

#### 에러 응답

해당 API에서 발생 가능한 에러는 필요에 따라 아래 형식으로 작성합니다:
```json
{
  "type": "https://api.bandchu.com/errors/user-email-duplicated",
  "title": "Conflict",
  "status": 409,
  "detail": "이미 사용 중인 이메일입니다.",
  "code": "USER_EMAIL_DUPLICATED",
  "timestamp": "2025-11-20T03:21:00.123Z",
  "path": "/api/signup"
}
```