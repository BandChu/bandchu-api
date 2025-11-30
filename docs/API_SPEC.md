# API 명세서

## 목차
- [공통 응답 포맷](#공통-응답-포맷)
- [에러 응답 포맷 (ProblemDetail)](#에러-응답-포맷-problemdetail)
- [회원 관리 API](#회원-관리-api-실제-스펙이-아닌-참고용-예시입니다)
    - [회원 가입](#회원-가입)
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

## 회원 관리 API (실제 스펙이 아닌 참고용 예시입니다!)

### 회원 가입

`POST /api/signup`

새로운 회원을 생성합니다.

#### 요청 헤더
```
Content-Type: application/json
Authorization: Bearer {token} (회원 가입에선 필요 없는데 다른 API 참고용으로 넣어둠!!)
```

#### 쿼리 파라미터
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
   /* 예시: 생성된 회원 정보 */
  },
  "message": "요청이 성공적으로 처리되었습니다."
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
  "instance": "/api/signup",
  "code": "USER_EMAIL_DUPLICATED",
  "timestamp": "2025-11-20T03:21:00.123Z"
}
```

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