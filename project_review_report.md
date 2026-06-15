# 🔍 인사장 (Insajang) 프로젝트 최종 보완점 및 면접 방어 리포트

> **본 문서는 프로젝트의 전체 소스 코드 분석을 바탕으로, 상용 서비스화 혹은 면접 단계에서 꼬리 질문으로 들어올 수 있는 기술적 보완점과 아키텍처 개선 로드맵을 정리한 가이드라인입니다.**

---

## 🛡️ 1. 보안성 관점 (Security & Credentials)

### 📌 JWT Secret Key 하드코딩 분리
- **문제점**: `JwtTokenProvider.java` 소스코드 내부에 암호화 시크릿 키가 평문으로 하드코딩되어 있어, 깃허브 등 공개 저장소 유출 시 심각한 보안 리스크가 있습니다.
- **하드코딩 위치**: [JwtTokenProvider.java](file:///c:/workspace/Insajang/insajang-backend/src/main/java/com/project/insajang/config/JwtTokenProvider.java#L22) (22라인)
  ```java
  private final String secretString = "contents-maker-studio-secure-key-2026-auth";
  ```
- **개선 로드맵**:
  - `application.properties`에 `jwt.secret=${JWT_SECRET_KEY}` 형태로 분리합니다.
  - 빌드 및 배포(Docker Compose, GitHub Actions) 단계에서 환경 변수로 주입되도록 설계하여 소스코드 노출 보안 사고를 예방합니다.

### 📌 데이터베이스 암호 외부 주입 설정
- **문제점**: `application.properties`에 `spring.datasource.password=1234`가 평문 하드코딩되어 있습니다.
- **하드코딩 위치**: [application.properties](file:///c:/workspace/Insajang/insajang-backend/src/main/resources/application.properties#L6) (6라인)
- **개선 로드맵**:
  - 개발망 외 배포 환경에서는 데이터베이스 패스워드 역시 환경 변수(예: `${DB_PASSWORD}`)로 오버라이드하도록 설정하여 민감 정보 무단 노출을 방지합니다.

---

## ⚡ 2. 스케줄러 및 비동기 처리 관점 (Performance & Scalability)

### 📌 예약 발행 알림(메일 발송)의 비동기화 (`@Async`)
- **문제점**: 
  - `ContentPublishScheduler.java`에서 1분마다 `SCHEDULED` 상태의 콘텐츠 리스트를 순차적 루프(`for`)로 처리하며 동기식으로 SMTP 메일을 발송합니다.
  - SMTP 메일 전송은 네트워크 I/O 병목이 커서(보통 건당 1~3초 소요), 특정 시간대에 사용자들이 대량으로 예약을 걸어두면 **루프 전체 지연 및 스케줄러 타임아웃**이 발생할 수 있습니다.
- **구현 파일 위치**: [ContentPublishScheduler.java](file:///c:/workspace/Insajang/insajang-backend/src/main/java/com/project/insajang/content/scheduler/ContentPublishScheduler.java#L27) (27라인)
- **개선 로드맵**:
  - Spring의 `@EnableAsync` 및 `@Async`를 도입하여 메일 발송용 스레드 풀(Thread Pool)을 구성하고 메일 전송 프로세스를 백그라운드로 완전히 위임합니다.
  - 스케줄러 스레드는 단순히 작업 큐에 던진 후 즉시 다음 루프로 진입하도록 개편하여 지연율을 0ms 수준으로 낮춥니다.

### 📌 예약 링크 도메인의 외부 환경 설정화
- **문제점**: 스케줄러 내부의 `publishLink` 생성 시 특정 도메인(`https://contentsmakerstudio.com`)이 하드코딩되어 배포 서버 도메인이 바뀔 경우 정상 동작하지 않을 여지가 있습니다.
- **하드코딩 위치**: [ContentPublishScheduler.java](file:///c:/workspace/Insajang/insajang-backend/src/main/java/com/project/insajang/content/scheduler/ContentPublishScheduler.java#L52) (52라인)
- **개선 로드맵**:
  - `application.properties`에 정의해 둔 `app.frontend-url` 값을 활용하여 동적으로 링크 주소를 생성하도록 코드를 수정합니다.

---

## 🔄 3. 운영 신뢰성 관점 (Reliability & Exception Handling)

### 📌 메일 발송 실패 시 무한 재시도(Infinity Retry) 루프 제어
- **문제점**: 
  - 만약 유효하지 않은 이메일 주소이거나 SMTP 일시 장애가 장기화될 경우, 스케줄러는 오류 로그만 남기고 상태를 `SCHEDULED`로 둡니다. 
  - 이로 인해 다음 1분 뒤 주기에 동일한 콘텐츠에 대해 **무한 재발송 시도**가 일어나 서버 자원과 메일 발송 쿼터가 낭비됩니다.
- **구현 파일 위치**: [ai_service.py](file:///c:/workspace/Insajang/insajang-python/ai_service.py#L75-L89) (75~89라인 - AI 호출 예외 대응 로직 포함)
- **개선 로드맵**:
  - `Content` 테이블이나 로그 테이블에 `retry_count` 컬럼을 신설합니다.
  - 발송 실패 시 count를 1씩 증가시키고, **최대 3~5회 도달 시 상태를 `FAILED_RETRY_EXCEEDED`** 같은 최종 에러 상태로 처리하여 무한 루프를 탈출(Circuit Breaker 전략)하도록 보완합니다.

---

## 🎨 4. 프론트엔드 최적화 관점 (Frontend Caching)

### 📌 대시보드 트리 데이터 클라이언트 캐싱
- **문제점**: 닉네임 중복 체크에는 디바운싱(Debounce)을 적용하여 트래픽을 80% 줄였으나, 메인 페이지 렌더링 시 콘텐츠 트리 데이터(`/api/contents/selectContentsTree`)를 매번 무조건 호출하는 상태입니다.
- **개선 로드맵**:
  - React Query(TanStack Query) 등을 도입해 갱신(Update/Delete)이 일어났을 때만 API를 호출하고(`Invalidate`), 그 외 정적 이동 시에는 로컬 캐시 데이터를 바라보게 유도하여 불필요한 중복 렌더링 쿼리를 제거합니다.
