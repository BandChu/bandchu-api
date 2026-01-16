# 공통 설정 도메인 개발 관련 공유 문서

## 개요
공통 설정 도메인(Common Config Domain)의 설정 관련 API를 구현했습니다. 프론트엔드에서 필요한 공통 설정 정보를 조회할 수 있는 엔드포인트를 제공합니다.

## 구현된 기능

### 1. Google Client ID 조회 API
- **엔드포인트**: `GET /api/config/google-client-id`
- **인증**: 불필요
- **기능**: 프론트엔드에서 Google OAuth 인증에 필요한 Client ID를 조회
- **요청**: 없음 (GET 요청)
- **응답**:
  ```json
  {
    "success": true,
    "data": {
      "clientId": "your-google-client-id-here.apps.googleusercontent.com"
    },
    "message": "Google Client ID를 성공적으로 조회했습니다."
  }
  ```
- **에러**: 없음 (항상 성공)
- **보안 고려사항**:
  - Google OAuth Client ID는 공개 정보로 설계되어 있어 공개 엔드포인트로 노출해도 안전합니다
  - 보안의 핵심은 서버에서 수행하는 ID Token 검증입니다
  - Client Secret은 사용하지 않으므로 노출 위험이 없습니다

## 수정/생성된 파일

### 공통 설정 도메인 (global/config)

#### Controller
- `src/main/kotlin/com/bandchu/api/global/config/ConfigController.kt` (신규)
  - Google Client ID 조회 엔드포인트 구현
  - 공개 엔드포인트로 설정 (인증 불필요)

#### DTO
- `GoogleClientIdResponse` (ConfigController 내부)
  - Google Client ID 응답 데이터 클래스

### 보안 설정 (global/config)

#### SecurityConfig
- `src/main/kotlin/com/bandchu/api/global/config/SecurityConfig.kt` (수정)
  - `/api/config/google-client-id` 엔드포인트를 공개 엔드포인트로 추가 (`permitAll()`)

## 보안 고려사항

### Google OAuth Client ID 공개
- **안전함**: Google OAuth 2.0 설계상 Client ID는 공개 정보입니다
- **이유**:
  1. 프론트엔드 JavaScript 코드에 하드코딩되어도 문제없습니다
  2. 보안의 핵심은 서버에서 수행하는 ID Token 검증입니다
  3. Client Secret을 사용하지 않으므로 노출 위험이 없습니다
- **추가 보안**: Google Cloud Console에서 Authorized JavaScript origins와 Authorized redirect URIs를 설정하여 추가 보안을 제공합니다

## 사용 예시

### 프론트엔드에서 Google Client ID 조회
```javascript
// Google OAuth 초기화 전에 Client ID 조회
const response = await fetch('https://api.example.com/api/config/google-client-id');
const data = await response.json();
const googleClientId = data.data.clientId;

// Google OAuth 초기화
// ... Google OAuth 설정에 clientId 사용
```

## 변경 이력

### 버전 1.0 (2025-12-04)
- Google Client ID 조회 API 추가
- ConfigController 생성
- SecurityConfig에 공개 엔드포인트 추가

---

**작성일**: 2025-12-04  
**작성자**: 신진수
**버전**: 1.0

