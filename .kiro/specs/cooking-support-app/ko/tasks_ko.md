# 구현 계획

- [ ] 1. 프로젝트 초기 설정
  - 백엔드(Spring Boot + Gradle)와 프론트엔드(React + TypeScript)의 프로젝트 구조 생성
  - 의존성 설정(jqwik, fast-check, i18next 등)
  - Clean Architecture에 기반한 디렉토리 구조 구축
  - _Requirements: 전체_

- [ ] 2. AWS 인프라스트럭처 설정
  - DynamoDB 테이블 생성(Users, Recipes, Schedules, ShoppingLists, Reviews)
  - S3 버킷 생성(이미지 스토리지)
  - Cognito 사용자 풀 설정
  - API Gateway 설정
  - _Requirements: 1.1, 1.2, 3.5, 11.5_

- [ ] 3. 사용자 관리 기능 구현
- [ ] 3.1 도메인 층: User 엔티티와 값 객체 구현
  - User 엔티티(userId, email, nickname, profileImageUrl, preferredLanguage, lastCookingDate)
  - 패스워드 검증 로직
  - _Requirements: 1.1, 11.1_

- [ ]* 3.2 속성 테스트: 계정 생성 성공
  - **Property 1: 계정 생성 성공**
  - **Validates: Requirements 1.1**

- [ ]* 3.3 속성 테스트: 패스워드 검증
  - **Property 44: 패스워드 검증**
  - **Validates: Requirements 11.1**

- [ ] 3.4 인프라 층: CognitoAuthService 구현
  - Cognito 통합(사용자 등록, 로그인, 토큰 관리)
  - 세션 관리(액세스 토큰 1시간, 리프레시 토큰 90일)
  - _Requirements: 1.2, 1.3, 11.5_

- [ ]* 3.5 속성 테스트: 인증 성공
  - **Property 2: 인증 성공**
  - **Validates: Requirements 1.2**

- [ ] 3.6 인프라 층: UserRepository 구현
  - DynamoDB 접근(CRUD 조작)
  - _Requirements: 1.1, 1.5_

- [ ] 3.7 애플리케이션 층: 사용자 관리 유스케이스 구현
  - 사용자 등록, 로그인, 프로필 업데이트, 계정 삭제
  - _Requirements: 1.1, 1.2, 1.5_

- [ ]* 3.8 속성 테스트: 계정 삭제 시 익명화
  - **Property 4: 계정 삭제 시 익명화**
  - **Validates: Requirements 1.5**

- [ ] 3.9 프레젠테이션 층: UserController 구현
  - REST API 엔드포인트(/api/users/*)
  - 요청/응답 DTO
  - 검증(프론트엔드·백엔드 양쪽)
  - _Requirements: 1.1, 1.2, 1.5_

- [ ] 3.10 프론트엔드: 인증 컴포넌트 구현
  - LoginPage, RegisterPage, PasswordResetPage
  - Redux 상태 관리(authSlice)
  - 폼 검증
  - _Requirements: 1.1, 1.2_

- [ ] 4. 프로필 이미지 관리 기능 구현
- [ ] 4.1 인프라 층: S3ImageService 구현
  - 이미지 업로드(pre-signed URL 사용)
  - 이미지 취득
  - 이미지 삭제
  - _Requirements: 1.4, 3.5_

- [ ] 4.2 애플리케이션 층: 이미지 검증 로직 구현
  - 파일 크기 체크(5MB 이하)
  - 형식 체크(JPEG, PNG)
  - _Requirements: 1.4, 3.5_

- [ ]* 4.3 속성 테스트: 프로필 이미지 검증
  - **Property 3: 프로필 이미지 검증**
  - **Validates: Requirements 1.4**

- [ ] 4.4 프레젠테이션 층: 이미지 업로드 엔드포인트 구현
  - POST /api/users/profile/image
  - POST /api/recipes/{id}/image
  - _Requirements: 1.4, 3.5_

- [ ] 4.5 프론트엔드: ImageUploader 컴포넌트 구현
  - 드래그 앤 드롭 지원
  - 미리보기 표시
  - 검증(크기, 형식)
  - _Requirements: 1.4_

- [ ] 5. 레시피 관리 기능 구현
- [ ] 5.1 도메인 층: Recipe 엔티티와 값 객체 구현
  - Recipe 엔티티(recipeId, title, authorId, ingredients, steps, cookingTime, imageUrl, isPublic, isDeleted)
  - Ingredient 값 객체(name, quantity, unit, note, optional)
  - 식재료 검증 로직
  - _Requirements: 3.1, 3.2_

- [ ]* 5.2 속성 테스트: 식재료 검증
  - **Property 10: 식재료 검증**
  - **Validates: Requirements 3.2**

- [ ] 5.3 인프라 층: RecipeRepository 구현
  - DynamoDB 접근(CRUD 조작)
  - GSI 검색(AuthorId, Category)
  - _Requirements: 2.1, 3.1, 3.3, 3.4_

- [ ] 5.4 애플리케이션 층: 레시피 관리 유스케이스 구현
  - 레시피 생성, 업데이트, 삭제, 검색
  - 삭제 시 참조 유지 로직
  - _Requirements: 2.1, 3.1, 3.3, 3.4_

- [ ]* 5.5 속성 테스트: 레시피 생성 성공
  - **Property 9: 레시피 생성 성공**
  - **Validates: Requirements 3.1**

- [ ] 5.6 프레젠테이션 층: RecipeController 구현
  - REST API 엔드포인트(/api/recipes/*)
  - 요청/응답 DTO
  - 검증(프론트엔드·백엔드 양쪽)
  - _Requirements: 2.1, 2.2, 3.1, 3.3, 3.4_

- [ ] 5.7 프론트엔드: 레시피 컴포넌트 구현
  - RecipeSearchPage, RecipeDetailPage, RecipeEditPage
  - Redux 상태 관리(recipeSlice)
  - 폼 검증
  - _Requirements: 2.1, 2.2, 3.1, 3.3_

- [ ] 6. AI 어드바이저 기능 구현
- [ ] 6.1 인프라 층: GeminiAPIService 구현
  - Gemini API 통합
  - 오류 처리(API 실패, 속도 제한)
  - _Requirements: 4.1, 4.4, 4.5_

- [ ] 6.2 인프라 층: CacheService 구현
  - 24시간 캐시 관리
  - 캐시 취득·저장·삭제
  - _Requirements: 4.2_

- [ ] 6.3 애플리케이션 층: AI 어드바이저 유스케이스 구현
  - AI 조언 취득
  - 캐시 폴백
  - 속도 제한 시 자동 재시도(30초 후)
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 6.4 프레젠테이션 층: AIAdvisorController 구현
  - POST /api/ai-advisor/advice
  - 오류 응답(캐시 유무, 속도 제한)
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 6.5 프론트엔드: AIAdvisorPanel 컴포넌트 구현
  - AI 조언 표시
  - 재시도 버튼
  - 속도 제한 배너
  - 정적 힌트 표시
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 7. 리뷰 기능 구현
- [ ] 7.1 도메인 층: Review 엔티티 구현
  - Review 엔티티(reviewId, recipeId, userId, rating, comment, status, reportedCount)
  - 신고 카운트 증가 로직
  - 자동 숨김 판정 로직
  - _Requirements: 5.1, 5.3, 5.4_

- [ ] 7.2 인프라 층: ReviewRepository 구현
  - DynamoDB 접근(CRUD 조작)
  - GSI 검색(UserId)
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 7.3 애플리케이션 층: 리뷰 관리 유스케이스 구현
  - 리뷰 생성, 업데이트, 삭제, 취득
  - 신고 처리
  - 자동 숨김 처리
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 7.4 프레젠테이션 층: ReviewController 구현
  - REST API 엔드포인트(/api/recipes/{id}/reviews, /api/reviews/*)
  - 요청/응답 DTO
  - 검증(프론트엔드·백엔드 양쪽)
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 7.5 프론트엔드: 리뷰 컴포넌트 구현
  - ReviewList, ReviewForm
  - Redux 상태 관리(reviewSlice)
  - 폼 검증
  - 신고 버튼
  - _Requirements: 5.1, 5.2, 5.3_

- [ ] 8. 일정 관리 기능 구현
- [ ] 8.1 도메인 층: Schedule 엔티티 구현
  - Schedule 엔티티(scheduleId, userId, date, type, recipeId, recipeTitle, memo)
  - 예정→실적 변환 로직
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 8.2 인프라 층: ScheduleRepository 구현
  - DynamoDB 접근(CRUD 조작)
  - 날짜 범위 검색
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 8.3 애플리케이션 층: 일정 관리 유스케이스 구현
  - 일정 생성, 업데이트, 삭제, 취득
  - 예정→실적 변환
  - LastCookingDate 업데이트
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 8.4 프레젠테이션 층: ScheduleController 구현
  - REST API 엔드포인트(/api/schedules/*)
  - 요청/응답 DTO
  - 검증(프론트엔드·백엔드 양쪽)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 8.5 프론트엔드: 일정 컴포넌트 구현
  - SchedulePage, CalendarView, ScheduleForm
  - Redux 상태 관리(scheduleSlice)
  - 캘린더 라이브러리 통합(react-calendar 등)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 9. 게으름 방지 알림 기능 구현
- [ ] 9.1 애플리케이션 층: 알림 판정 로직 구현
  - LastCookingDate로부터 3일 경과 판정(4일째 0시)
  - 알림 메시지 무작위 선택(경고/격려)
  - _Requirements: 7.1, 7.4_

- [ ] 9.2 프레젠테이션 층: 알림 판정 엔드포인트 구현
  - GET /api/schedules/alert
  - _Requirements: 7.1_

- [ ] 9.3 프론트엔드: AlertModal 컴포넌트 구현
  - 모달 표시
  - localStorage에 의한 재표시 제어
  - 빠른 요리 등록 버튼
  - _Requirements: 7.1, 7.2, 7.3_

- [ ] 10. 장보기 목록 관리 기능 구현
- [ ] 10.1 도메인 층: ShoppingListItem 엔티티 구현
  - ShoppingListItem 엔티티(itemId, userId, name, quantity, unit, isChecked, isCheckedAt, normalizedKey)
  - 정규화 키 생성 로직
  - 자동 삭제 판정 로직(3일 경과)
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 10.2 인프라 층: ShoppingListRepository 구현
  - DynamoDB 접근(CRUD 조작)
  - 정규화 키에 의한 검색
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 10.3 애플리케이션 층: 장보기 목록 관리 유스케이스 구현
  - 항목 추가(수량 합산 로직)
  - 항목 업데이트, 삭제, 취득
  - 자동 삭제 처리(배치 작업)
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 10.4 프레젠테이션 층: ShoppingListController 구현
  - REST API 엔드포인트(/api/shopping-lists/*)
  - 요청/응답 DTO
  - 검증(프론트엔드·백엔드 양쪽)
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 10.5 프론트엔드: 장보기 목록 컴포넌트 구현
  - ShoppingListPage, ShoppingListItem
  - Redux 상태 관리(shoppingListSlice)
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 11. 다국어 지원 기능 구현
- [ ] 11.1 백엔드: 다국어 메시지 파일 생성
  - messages_ja.properties(일본어)
  - messages_ko.properties(한국어)
  - 오류 메시지, 검증 메시지
  - _Requirements: 9.3, 9.4_

- [ ] 11.2 백엔드: Accept-Language 헤더 처리 구현
  - 요청 헤더에서 언어 취득
  - 메시지 소스에서 적절한 언어의 메시지 반환
  - _Requirements: 9.4_

- [ ] 11.3 프론트엔드: i18next 설정 및 로케일 파일 생성
  - i18n.ts 설정
  - ja.json(일본어 번역)
  - ko.json(한국어 번역)
  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 11.4 프론트엔드: LanguageSelector 컴포넌트 구현
  - 언어 선택 UI
  - PreferredLanguage 저장
  - _Requirements: 9.1_

- [ ] 12. 관리자 기능 구현
- [ ] 12.1 애플리케이션 층: 관리자 유스케이스 구현
  - 사용자 관리(정지, 삭제)
  - 레시피 관리(심사, 상태 변경, 삭제)
  - 통계 정보 취득
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 12.2 프레젠테이션 층: AdminController 구현
  - REST API 엔드포인트(/api/admin/*)
  - 관리자 권한 체크
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 12.3 프론트엔드: 관리자 컴포넌트 구현
  - AdminDashboard, UserManagement, RecipeManagement
  - 관리자 전용 라우팅
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 13. 오류 처리 및 로깅 구현
- [ ] 13.1 백엔드: 글로벌 예외 핸들러 구현
  - 검증 오류(400)
  - 인증 오류(401)
  - 인가 오류(403)
  - 리소스 미검출 오류(404)
  - 서버 오류(500)
  - _Requirements: 12.5_

- [ ] 13.2 백엔드: CloudWatch 로깅 구현
  - INFO 레벨 이상의 로그 기록
  - 오류 상세 로그 기록
  - _Requirements: 12.4_

- [ ] 13.3 프론트엔드: 오류 배너 컴포넌트 구현
  - 화면 상단에 오류 메시지 표시
  - 재시도 버튼
  - _Requirements: 12.5_

- [ ] 14. 대시보드 및 홈 화면 구현
- [ ] 14.1 프론트엔드: DashboardPage 컴포넌트 구현
  - 홈 화면 레이아웃
  - 최근 레시피 표시
  - 일정 개요 표시
  - 장보기 목록 개요 표시
  - _Requirements: 전체_

- [ ] 14.2 프론트엔드: 공통 컴포넌트 구현
  - Header, Footer
  - LoadingSkeleton
  - ErrorBanner
  - _Requirements: 전체_

- [ ] 15. 최종 체크포인트 - 모든 테스트가 합격하는지 확인
  - 모든 테스트가 합격하는지 확인하고 질문이 있으면 사용자에게 문의
  - _Requirements: 전체_
