# 설계 문서

## 개요

자취 지원 및 식비 절약 애플리케이션은 사용자가 요리 습관을 유지하고 식비를 절약하기 위한 웹 애플리케이션입니다. 본 시스템은 레시피 관리, 일정 관리, 장보기 목록 관리, AI 어드바이저 기능을 제공하여 사용자의 자취를 지원합니다.

시스템은 Clean Architecture를 채택한 백엔드(Java + Spring Boot + Gradle), 모던한 프론트엔드(TypeScript + React + Redux), AWS 서버리스 인프라(DynamoDB, S3, Cognito, Lambda)로 구성됩니다.

주요 설계 목표:
- 사용자 친화적인 UI/UX
- 빠른 응답(요청의 90%를 2초 이내)
- 확장 가능한 아키텍처
- 안전한 인증·인가
- 다국어 지원(일본어·한국어)

## 아키텍처

### 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  React + TypeScript + Redux                          │   │
│  │  - UI Components                                     │   │
│  │  - State Management                                  │   │
│  │  - i18n (일본어/한국어)                              │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │ HTTPS (TLS 1.2+)
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (AWS)                       │
│  - REST API Endpoints                                        │
│  - Request Validation                                        │
│  - Rate Limiting                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Backend Layer (Lambda)                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Spring Boot + Java + Gradle                         │   │
│  │  Clean Architecture:                                 │   │
│  │  ├─ Presentation Layer (Controllers)                 │   │
│  │  ├─ Application Layer (Use Cases)                    │   │
│  │  ├─ Domain Layer (Entities, Business Logic)          │   │
│  │  └─ Infrastructure Layer (Repositories, External)    │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ↓                   ↓                   ↓
┌──────────────┐  ┌──────────────────┐  ┌──────────────┐
│   DynamoDB   │  │   S3 Storage     │  │   Cognito    │
│  (Database)  │  │   (Images)       │  │   (Auth)     │
└──────────────┘  └──────────────────┘  └──────────────┘
                            │
                            ↓
                  ┌──────────────────┐
                  │  Gemini API      │
                  │  (AI Advisor)    │
                  └──────────────────┘
```

### 레이어 구성(Clean Architecture)

**1. Presentation Layer(프레젠테이션 층)**
- REST API 컨트롤러
- 요청/응답 DTO
- 입력 검증
- 다국어 지원(Accept-Language 헤더 처리)

**2. Application Layer(애플리케이션 층)**
- 유스케이스 구현
- 비즈니스 로직 오케스트레이션
- 트랜잭션 관리

**3. Domain Layer(도메인 층)**
- 엔티티(User, Recipe, Schedule, ShoppingList, Review)
- 도메인 로직
- 비즈니스 규칙

**4. Infrastructure Layer(인프라스트럭처 층)**
- DynamoDB 리포지토리 구현
- S3 스토리지 서비스
- Cognito 인증 서비스
- Gemini API 통합
- 캐시 관리

## 컴포넌트 및 인터페이스

### 백엔드 컴포넌트

**1. User Management Module**
- UserController: 사용자 등록, 로그인, 프로필 관리
- UserService: 사용자 관련 비즈니스 로직
- UserRepository: DynamoDB 접근
- CognitoAuthService: 인증·인가

**2. Recipe Management Module**
- RecipeController: 레시피 CRUD 조작
- RecipeService: 레시피 검색, 검증
- RecipeRepository: DynamoDB 접근
- S3ImageService: 이미지 업로드/취득

**3. Schedule Management Module**
- ScheduleController: 일정 CRUD 조작
- ScheduleService: 예정/실적 관리, 알림 판정
- ScheduleRepository: DynamoDB 접근

**4. Shopping List Module**
- ShoppingListController: 장보기 목록 CRUD 조작
- ShoppingListService: 수량 합산, 자동 삭제
- ShoppingListRepository: DynamoDB 접근

**5. Review Module**
- ReviewController: 리뷰 CRUD 조작
- ReviewService: 신고 처리, 자동 숨김
- ReviewRepository: DynamoDB 접근

**6. AI Advisor Module**
- AIAdvisorController: AI 조언 취득
- AIAdvisorService: Gemini API 호출, 캐시 관리
- CacheService: 24시간 캐시

**7. Admin Module**
- AdminController: 관리자 기능
- AdminService: 사용자 관리, 레시피 심사
- AdminRepository: DynamoDB 접근

### 프론트엔드 컴포넌트

**1. Authentication Components**
- LoginPage: 로그인 화면
- RegisterPage: 사용자 등록 화면
- PasswordResetPage: 패스워드 재설정 화면

**2. Dashboard Components**
- DashboardPage: 홈 화면
- AlertModal: 게으름 방지 알림

**3. Recipe Components**
- RecipeSearchPage: 레시피 검색 화면
- RecipeDetailPage: 레시피 상세 화면
- RecipeEditPage: 레시피 편집 화면
- AIAdvisorPanel: AI 어드바이저 패널
- ReviewList: 리뷰 목록
- ReviewForm: 리뷰 게시 폼

**4. Schedule Components**
- SchedulePage: 일정 관리 화면
- CalendarView: 캘린더 표시
- ScheduleForm: 예정/실적 등록 폼

**5. Shopping List Components**
- ShoppingListPage: 장보기 목록 화면
- ShoppingListItem: 목록 항목

**6. Profile Components**
- ProfilePage: 프로필 편집 화면
- ImageUploader: 이미지 업로드 컴포넌트
- LanguageSelector: 언어 선택

**7. Admin Components**
- AdminDashboard: 관리 대시보드
- UserManagement: 사용자 관리
- RecipeManagement: 레시피 관리

## 데이터 모델

### DynamoDB 테이블 설계

**Users 테이블**
```
Partition Key: UserId (String, UUID)
Attributes:
  - Email (String)
  - Nickname (String)
  - DisplayName (String)
  - ProfileImageUrl (String)
  - PreferredLanguage (String: "ja" | "ko")
  - CreatedAt (String, ISO8601)
  - LastCookingDate (String, ISO8601)
  - LastLoginDate (String, ISO8601)
  - Timezone (String, default: "Asia/Tokyo")
  - MarketingOptOut (Boolean)
```

**Recipes 테이블**
```
Partition Key: RecipeId (String, UUID)
Sort Key: CreatedAt (String, ISO8601)
GSI_Author: AuthorId (PK)
GSI_Category: Category (PK)
Attributes:
  - Title (String)
  - AuthorId (String)
  - Ingredients (List<Map>)
    - name (String)
    - quantity (Number)
    - unit (String)
    - note (String, optional)
    - optional (Boolean, optional)
  - Steps (List<String>)
  - CookingTime (Number, minutes)
  - TotalTimeMin (Number, minutes)
  - ServingsDefault (Number)
  - ImageUrl (String)
  - IsPublic (Boolean)
  - IsDeleted (Boolean)
  - CreatedAt (String, ISO8601)
  - UpdatedAt (String, ISO8601)
```

**Schedules 테이블**
```
Partition Key: UserId (String)
Sort Key: Date#Type#RecipeId (String)
Attributes:
  - Date (String, YYYY-MM-DD)
  - Type (String: "PLANNED" | "COOKED")
  - RecipeId (String)
  - RecipeTitle (String)
  - Memo (String, max 120 chars)
  - CreatedAt (String, ISO8601)
```

**ShoppingLists 테이블**
```
Partition Key: UserId (String)
Sort Key: ItemId (String, UUID)
Attributes:
  - Name (String)
  - Quantity (Number)
  - Unit (String)
  - IsChecked (Boolean)
  - IsCheckedAt (String, ISO8601)
  - AddedAt (String, ISO8601)
  - SourceRecipeId (String, optional)
  - NormalizedKey (String)
```

**Reviews 테이블**
```
Partition Key: RecipeId (String)
Sort Key: ReviewId (String, UUID)
GSI_User: UserId (PK)
Attributes:
  - UserId (String)
  - Rating (Number, 1-5)
  - Comment (String, max 300 chars)
  - Status (String: "visible" | "hidden")
  - ReportedCount (Number)
  - CreatedAt (String, ISO8601)
  - UpdatedAt (String, ISO8601)
```

## 검증 전략

### 검증 방침

본 시스템에서는 프론트엔드와 백엔드 양쪽에서 검증을 실시하여 다층 방어를 실현합니다.

**프론트엔드 검증**:
- 목적: 사용자 경험 향상, 즉각적인 피드백
- 타이밍: 실시간(입력 중) 및 폼 제출 시
- 구현: React Hook Form 또는 Formik 사용
- 대상:
  - 필수 필드 체크
  - 문자 수 제한(예: 레시피 제목, 댓글)
  - 형식 검증(예: 이메일 주소, 패스워드 강도)
  - 숫자 범위 체크(예: 식재료 수량 0~9999)
  - 파일 크기와 형식(예: 이미지 5MB 이하, JPEG/PNG)

**백엔드 검증**:
- 목적: 보안, 데이터 정합성 보증
- 타이밍: API 요청 수신 시
- 구현: Spring Boot Validation(JSR-380) 사용
- 대상:
  - 모든 프론트엔드 검증 항목 재검증
  - 비즈니스 규칙 검증(예: 레시피 삭제 권한, 리뷰 편집 권한)
  - 데이터베이스 정합성 체크(예: 중복 체크, 외부 키 제약)
  - 인증·인가 체크

## 정확성 속성

*속성은 시스템의 모든 유효한 실행에서 참이어야 하는 특성 또는 동작입니다. 속성은 사람이 읽을 수 있는 사양과 기계가 검증할 수 있는 정확성 보증 사이의 다리 역할을 합니다.*

### Property 1: 계정 생성 성공

*모든* 유효한 이메일 주소와 패스워드 요건을 충족하는 패스워드에 대해, 계정 생성은 성공하고 Cognito에 사용자가 등록된다

**Validates: Requirements 1.1**

### Property 2: 인증 성공

*모든* 유효한 인증 정보에 대해, 인증은 성공하고 사용자는 시스템에 접근할 수 있다

**Validates: Requirements 1.2**

### Property 3: 프로필 이미지 검증

*모든* 이미지 업로드에 대해, 파일 크기가 5MB 이하이고 형식이 JPEG 또는 PNG인 경우에만 업로드가 성공한다

**Validates: Requirements 1.4**

### Property 4: 계정 삭제 시 익명화

*모든* 사용자에 대해, 계정 삭제 시 계정과 프로필 이미지는 삭제되고 게시한 레시피와 리뷰는 익명화된다

**Validates: Requirements 1.5**

### Property 5: 레시피 검색 일치

*모든* 검색 조건에 대해, 반환되는 레시피는 지정된 조건(카테고리, 식재료, 조리 시간)에 일치한다

**Validates: Requirements 2.1**

_[추가 속성은 일본어 버전과 동일한 구조로 계속됩니다...]_

## 오류 처리

### 오류 분류

**1. 검증 오류(400 Bad Request)**
- 입력값이 요건을 충족하지 않는 경우
- 예: 패스워드가 8자 미만, 이미지 크기가 5MB 초과
- 대응: 상세한 오류 메시지를 반환하고 프론트엔드에서 필드 단위로 표시

**2. 인증 오류(401 Unauthorized)**
- 인증 정보가 무효하거나 만료된 경우
- 대응: 로그인 화면으로 리디렉션

**3. 인가 오류(403 Forbidden)**
- 리소스에 대한 접근 권한이 없는 경우
- 예: 타인의 레시피를 편집하려고 한 경우
- 대응: 오류 메시지를 표시하고 이전 화면으로 돌아감

**4. 리소스 미검출 오류(404 Not Found)**
- 지정된 리소스가 존재하지 않는 경우
- 예: 삭제된 레시피에 접근
- 대응: "삭제된 레시피입니다" 메시지 표시

**5. 서버 오류(500 Internal Server Error)**
- 시스템 내부 오류
- 대응: 범용 오류 메시지를 표시하고 CloudWatch에 로그 기록

## 테스트 전략

### 테스트 방침

본 시스템에서는 유닛 테스트와 속성 기반 테스트 양쪽을 실시하여 포괄적인 테스트 커버리지를 확보합니다.

**유닛 테스트**:
- 특정 예나 엣지 케이스를 검증
- 통합 포인트의 동작 확인
- 커버리지 목표: 50% 이상(중요 로직 우선)

**속성 기반 테스트**:
- 보편적인 속성을 검증
- 다양한 입력에 대한 정확성 보증
- 각 속성은 최소 100회 반복 실행

### 속성 기반 테스트 라이브러리

**백엔드(Java)**:
- **jqwik** - Java용 속성 기반 테스트 라이브러리
- Gradle 의존성:
  ```gradle
  testImplementation 'net.jqwik:jqwik:1.7.4'
  ```

**프론트엔드(TypeScript)**:
- **fast-check** - TypeScript/JavaScript용 속성 기반 테스트 라이브러리
- npm 의존성:
  ```json
  "devDependencies": {
    "fast-check": "^3.13.0"
  }
  ```

## 빌드 및 배포

### 빌드 도구

**백엔드**:
- **Gradle** - Java/Spring Boot 프로젝트의 빌드 도구
- 버전: Gradle 8.x
- 주요 태스크:
  - `./gradlew build` - 프로젝트 빌드
  - `./gradlew test` - 테스트 실행
  - `./gradlew bootJar` - 실행 가능 JAR 생성

**프론트엔드**:
- **npm** - Node.js 패키지 매니저
- 주요 명령:
  - `npm install` - 의존성 설치
  - `npm run build` - 프로덕션 빌드
  - `npm test` - 테스트 실행

### 배포 전략

**인프라스트럭처**:
- AWS SAM(Serverless Application Model) 또는 AWS CDK 사용
- Lambda 함수로 백엔드 배포
- CloudFront + S3로 프론트엔드 배포

**배포 플로우**:
1. main 브랜치로의 머지를 트리거
2. CI/CD 파이프라인(GitHub Actions / AWS CodePipeline) 기동
3. 백엔드: Gradle 빌드 → Lambda 함수 배포
4. 프론트엔드: npm 빌드 → S3 업로드 → CloudFront 무효화
5. DynamoDB 테이블, S3 버킷, Cognito 사용자 풀은 사전에 프로비저닝
