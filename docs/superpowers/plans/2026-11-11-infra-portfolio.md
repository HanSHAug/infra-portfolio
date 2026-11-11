# 인프라·백엔드 겸용 포트폴리오 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 코드를 올리면 자동으로 테스트·빌드·AWS 배포되고, Grafana로 상태를 보며, 장애 대응 기록이 남아 있는 공개 저장소 1개를 10주 안에 완성한다.

**Architecture:** Spring Boot 서비스(Atlas) 하나를 Docker로 묶고, GitHub Actions가 테스트·이미지 생성·배포를 맡고, Terraform이 AWS 서버를 만들고 지운다. Prometheus가 지표를 모으고 Grafana가 보여준다.

**Tech Stack:** Java 21, Spring Boot 3, Gradle, PostgreSQL, H2(테스트·로컬), Docker Compose, GitHub Actions + GHCR, Terraform, AWS EC2(ap-northeast-2), Prometheus, Grafana

**Spec:** `docs/superpowers/specs/2026-11-11-infra-portfolio-design.md`

## Global Constraints

- 주 22시간 × 10주 = 약 220시간. 일정은 기준선이며 주차 끝마다 학습 일지를 보고 조정한다.
- 줄이는 순서: (1) Atlas 기능, (2) 장애 문서 3건 → 2건, (3) Terraform 범위(서버 1대만). 모니터링과 CI/CD는 마지막까지 지킨다.
- 클라우드는 AWS, 프리티어 범위의 작은 사양만 쓴다.
- 실습이 끝나면 매번 `terraform destroy`로 전부 삭제하고, 삭제 확인을 완료 기준에 포함한다.
- 저장소는 공개를 전제로 한다. AWS 키와 DB 비밀번호는 코드에 넣지 않는다 (로컬 `.env`, 자동 배포 GitHub Secrets).
- 저장소 생성 첫날 `.gitignore`에 `.env`와 Terraform 상태 파일을 넣는다.
- Atlas 기능은 장애 기록의 등록, 목록 조회, 단건 조회, 키워드 검색만. 기록 항목은 제목, 심각도, 증상, 원인, 해결, 작성일.
- 제외: 쿠버네티스, AI 인프라, 보안 심화, Vault/MinIO, Ansible.
- 모든 명령은 WSL2 Ubuntu 터미널에서 실행한다.
- 본인이 설명할 수 있는 것만 남긴다. 모든 Task의 마지막 커밋 전에 `docs/learning-log.md`에 배운 것·막혔던 것을 3~5줄 추가한다.

## Review Focus

1. `.env`가 실수로 git에 올라간다 → 어떤 상황에서도 추적되지 않아야 한다. (Task 1)
2. `terraform destroy` 뒤에 서버·디스크가 남아 요금이 나간다 → 실행 중 리소스가 0개여야 한다. (Task 5)
3. 존재하지 않는 id로 조회한다 → 500이 아니라 404. (Task 2)
4. 제목이 비었거나 200자를 넘고, 심각도 값이 잘못되었다 → 500이 아니라 400. (Task 2)
5. 검색어가 공백이거나 한글·대소문자가 섞여 있다 → 에러나 0건이 아니라 공백은 전체 목록, 한글·대소문자는 정상 검색. (Task 2)

---

### Task 1: 환경과 저장소 (1주차)

**Files:**
- Create: `.gitignore`, `README.md`, `docs/learning-log.md`, `docs/superpowers/specs/2026-11-11-infra-portfolio-design.md`, `docs/superpowers/plans/2026-11-11-infra-portfolio.md`

**Interfaces:**
- Produces: 공개 GitHub 저장소(main 브랜치), 이후 모든 Task가 쓰는 `.gitignore` 규칙

- [ ] **Step 1: 도구 확인.** WSL2 Ubuntu에서 `java -version`(21), `git --version`, `gh --version` 확인. 없으면 설치. 기대 출력: Java 버전이 21로 시작.
- [ ] **Step 2: 저장소 이름을 정하고** GitHub에 Public 저장소를 만든 뒤 WSL2에 clone한다 (Windows 폴더가 아니라 WSL2 홈 아래).
- [ ] **Step 3: `.gitignore`에 다음을 넣는다:** `.env`, `*.tfstate`, `*.tfstate.*`, `.terraform/`, `*.tfvars`, `build/`, `.gradle/`, `.idea/`
- [ ] **Step 4: Review Focus 1 확인.** `echo "SECRET=x" > .env && git status --short` 에 `.env`가 나타나지 않고, `git check-ignore -v .env`가 규칙 줄을 출력해야 한다. 확인 후 `.env` 삭제.
- [ ] **Step 5: spec·plan 문서를 위 경로에 복사하고**, `README.md`에 제목과 한 줄 소개만 쓰고, `docs/learning-log.md`를 만든다.
- [ ] **Step 6: Commit & push.** `git add . && git commit -m "chore: init repository" && git push`. 기대: GitHub에서 파일이 보임.

### Task 2: Atlas 서비스 (1~2주차)

**Files:**
- Create: Spring Initializr 결과물을 저장소 루트에 풀어 `build.gradle`, `src/...` 생성 (Java 21, Gradle, Boot 3, 의존성 Web·Data JPA·Validation·PostgreSQL Driver·Actuator; 테스트용 H2)
- Create: `src/main/java/com/atlas/incident/{Severity,Incident,IncidentRequest,IncidentResponse,IncidentRepository,IncidentService,IncidentController,IncidentNotFoundException,ApiExceptionHandler}.java`
- Create: `src/main/resources/application.yml`(PostgreSQL, 환경변수 `DB_URL`·`DB_USER`·`DB_PASSWORD`), `application-local.yml`(H2 메모리)
- Test: `src/test/java/com/atlas/incident/IncidentControllerTest.java`

**Interfaces:**
- Produces:
  - `enum Severity { LOW, MEDIUM, HIGH, CRITICAL }`
  - `record IncidentRequest(@NotBlank @Size(max=200) String title, @NotNull Severity severity, @NotBlank String symptom, String cause, String resolution)`
  - `record IncidentResponse(Long id, String title, Severity severity, String symptom, String cause, String resolution, Instant createdAt)`
  - `IncidentService`: `create(IncidentRequest): IncidentResponse`, `list(): List<IncidentResponse>`(최신순), `get(Long): IncidentResponse`(없으면 `IncidentNotFoundException`), `search(String): List<IncidentResponse>`(제목·증상에서 대소문자 무시 부분 일치, 공백이면 `list()`)
  - REST: `POST /api/incidents`(201), `GET /api/incidents?keyword=`, `GET /api/incidents/{id}`(없으면 404 JSON), 검증 실패는 400 JSON

- [ ] **Step 1: 프로젝트를 만들고** `./gradlew test`로 기본 `contextLoads`가 통과하는지 확인한다 (테스트는 `local` 프로파일의 H2 사용).
- [ ] **Step 2: `IncidentControllerTest`에 실패하는 테스트를 쓴다** (`@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("local")`):
  - `createIncident_returns201_withBody`: 유효한 JSON(severity `"HIGH"`) POST → 201, `$.id` 존재, `$.severity` == `"HIGH"`, `$.createdAt` 존재
  - `createIncident_blankTitle_returns400`, `createIncident_titleOver200_returns400` (201자 제목), `createIncident_invalidSeverity_returns400` (severity `"SEVERE"`)
  - `getIncident_unknownId_returns404`: `GET /api/incidents/999999` → 404
  - `getIncident_existing_returns200`
  - `list_returnsNewestFirst`: 2건 등록 후 첫 요소가 나중에 등록한 것
  - `search_blankKeyword_returnsAll`: `keyword=%20` → 전체 건수
  - `search_koreanAndCase_matches`: "DB 연결 실패"와 "Nginx 502" 등록 후 `keyword=db` → 1건, `keyword=연결` → 1건, `keyword=NGINX` → 1건
- [ ] **Step 3: `./gradlew test --tests '*IncidentControllerTest'` 실행.** 기대: 컴파일 실패 또는 FAIL.
- [ ] **Step 4: 위 Interfaces의 시그니처대로 구현한다.** 검색은 Repository의 파생 쿼리(`findByTitleContainingIgnoreCaseOrSymptomContainingIgnoreCase`)로 구현. 예외 처리는 `@RestControllerAdvice`에서 404·400을 JSON `{ "message": ... }`로 변환.
- [ ] **Step 5: 같은 명령 재실행.** 기대: 전부 PASS.
- [ ] **Step 6: 로컬 실행 확인.** `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun` 후 `curl -s -X POST localhost:8080/api/incidents -H 'Content-Type: application/json' -d '{"title":"DB 연결 실패","severity":"HIGH","symptom":"502"}'` → 201 응답, `curl -s 'localhost:8080/api/incidents?keyword=연결'` → 1건.
- [ ] **Step 7: Commit & push.** `git commit -m "feat: add Atlas incident API"` (learning-log 추가 포함).

### Task 3: Docker (3주차)

**Files:**
- Create: `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `.env.example`

**Interfaces:**
- Consumes: Task 2의 환경변수 `DB_URL`, `DB_USER`, `DB_PASSWORD`, 포트 8080
- Produces: compose 서비스 `app`(8080), `db`(PostgreSQL, 볼륨으로 데이터 유지); `.env.example`(가짜 값만)

- [ ] **Step 1: Docker Desktop의 WSL2 연동을 켜고** `docker --version`, `docker compose version` 확인.
- [ ] **Step 2: `Dockerfile`** — 멀티스테이지(빌드: JDK 21, 실행: JRE 21), 일반 사용자로 실행.
- [ ] **Step 3: `docker-compose.yml`** — `db`에 healthcheck, `app`은 `db`가 healthy일 때 시작, DB 볼륨 설정. 비밀번호는 `.env`에서 읽는다. `.env.example`을 복사해 `.env`를 만든다.
- [ ] **Step 4: 검증.** `docker compose up -d --build` 후 `curl -s localhost:8080/actuator/health` → `{"status":"UP"}`. 기록을 1건 등록하고 `docker compose down && docker compose up -d` 후에도 조회되면 데이터 유지 성공.
- [ ] **Step 5: Commit & push** (`git status`에 `.env`가 없는지 확인).

### Task 4: CI (4~5주차)

**Files:**
- Create: `.github/workflows/ci.yml`

**Interfaces:**
- Consumes: Task 2의 `./gradlew test`, Task 3의 `Dockerfile`
- Produces: GHCR 이미지 `ghcr.io/<github-id>/<repo>:<git-sha>`와 `:latest`(main에서만)

- [ ] **Step 1: `test` 잡** — push·PR마다 JDK 21 설치 후 `./gradlew test`.
- [ ] **Step 2: 일부러 테스트 하나를 깨뜨린 브랜치를 push해** Actions가 빨간불인지 확인한 뒤 되돌린다. 기대: 실패 → 수정 후 성공을 모두 경험.
- [ ] **Step 3: `build-and-push` 잡** — main에서만, `test` 성공 후, `GITHUB_TOKEN`으로 GHCR에 로그인해 이미지를 push.
- [ ] **Step 4: 검증.** main에 push 후 Actions 전체 초록, 저장소 Packages에 이미지가 보임. README에 CI 배지 추가.
- [ ] **Step 5: Commit & push.**

### Task 5: Terraform + AWS 서버 (6주차)

**Files:**
- Create: `infra/terraform/{versions.tf,main.tf,variables.tf,outputs.tf,terraform.tfvars.example}`, `docs/checklists/terraform-apply-destroy.md`

**Interfaces:**
- Produces: 변수 `my_ip_cidr`(string, 필수), `instance_type`(기본 프리티어 대상 사양); 출력 `public_ip`; EC2(Ubuntu, ap-northeast-2, user_data로 Docker·Compose 설치), 보안 그룹은 22·8080·3000 모두 `my_ip_cidr`에서만 허용

- [ ] **Step 1: AWS 계정을 만들고** 루트 대신 IAM 사용자를 쓴다. 액세스 키는 `aws configure`로 로컬에만 저장한다. 즉시 **월 5달러 예산 알림**을 만든다. 검증: `aws sts get-caller-identity`가 응답하고, 예산 알림 확인 메일이 도착.
- [ ] **Step 2: Terraform 설치 후** `terraform version` 확인.
- [ ] **Step 3: 위 Interfaces대로 `main.tf` 등을 작성한다.** `terraform.tfvars`는 `.gitignore` 대상이며, 저장소에는 `.example`만 둔다.
- [ ] **Step 4: `terraform init && terraform plan -var my_ip_cidr=<내IP>/32`.** 기대: `Plan: N to add, 0 to change, 0 to destroy`.
- [ ] **Step 5: `terraform apply` 후** 출력된 `public_ip`로 ssh 접속해 `docker --version` 확인.
- [ ] **Step 6: `terraform destroy` 후 Review Focus 2 확인.** 기대: `Destroy complete!`, 그리고 `aws ec2 describe-instances --region ap-northeast-2 --filters Name=instance-state-name,Values=pending,running --query 'Reservations[].Instances[].InstanceId'` 와 `aws ec2 describe-volumes --region ap-northeast-2 --query 'Volumes[].VolumeId'` 와 `aws ec2 describe-addresses --region ap-northeast-2 --query 'Addresses[].PublicIp'` 가 모두 `[]`.
- [ ] **Step 7: `docs/checklists/terraform-apply-destroy.md`** 에 만들기 → 확인 → 삭제 순서와 위 검증 명령을 정리한다 (내 IP가 바뀌면 `my_ip_cidr`를 갱신한다는 메모 포함).
- [ ] **Step 8: Commit & push** (`terraform.tfvars`, `*.tfstate`가 올라가지 않았는지 `git status`로 확인).

### Task 6: 자동 배포 (7주차)

**Files:**
- Create: `.github/workflows/deploy.yml`, `docker-compose.prod.yml`
- Modify: `docs/checklists/terraform-apply-destroy.md` (배포 후 확인 단계 추가)

**Interfaces:**
- Consumes: Task 4의 GHCR 이미지, Task 5의 `public_ip`
- Produces: GitHub Secrets `EC2_HOST`, `EC2_SSH_KEY`, `GHCR_TOKEN`(읽기 전용); `docker-compose.prod.yml`은 GHCR 이미지를 쓰고 비밀번호는 서버의 `.env`에서 읽음

- [ ] **Step 1: Task 5로 서버를 다시 만들고**, 서버에 `.env`를 손으로 만든다 (저장소에 올리지 않음). GitHub Secrets에 세 값을 등록한다.
- [ ] **Step 2: `deploy.yml`** — Task 4의 `build-and-push` 성공 후 main에서만 실행. SSH로 접속해 `docker compose -f docker-compose.prod.yml pull && up -d`.
- [ ] **Step 3: 검증.** 응답 필드를 하나 바꾸는 작은 변경을 main에 push → Actions 초록 → `curl -s http://<public_ip>:8080/actuator/health` → `UP`이고 변경된 응답이 보임.
- [ ] **Step 4: `terraform destroy`와 Task 5 Step 6의 검증 명령을 다시 실행** (모두 `[]`). 서버를 새로 만들면 `EC2_HOST` Secret을 갱신해야 한다는 점을 체크리스트에 적는다.
- [ ] **Step 5: Commit & push.**

### Task 7: 모니터링 (8주차)

**Files:**
- Create: `monitoring/prometheus.yml`, `monitoring/grafana/provisioning/datasources/prometheus.yml`, `monitoring/grafana/provisioning/dashboards/dashboards.yml`, `monitoring/grafana/dashboards/atlas.json`
- Modify: `build.gradle`(`micrometer-registry-prometheus`), `application.yml`(`management.endpoints.web.exposure.include=health,prometheus`), `docker-compose.yml`·`docker-compose.prod.yml`(prometheus·grafana 서비스, Grafana 관리자 비밀번호는 `GRAFANA_ADMIN_PASSWORD` 환경변수)
- Test: `src/test/java/com/atlas/MetricsEndpointTest.java`

**Interfaces:**
- Consumes: Task 3의 compose 서비스 `app`(8080)
- Produces: `/actuator/prometheus`; Prometheus 스크랩 job `atlas`(target `app:8080`); Grafana(3000) 패널 5개 — 요청 수/초, 5xx 비율, 응답시간 p95, JVM 메모리, 서비스 up

- [ ] **Step 1: 실패하는 테스트.** `MetricsEndpointTest.prometheusEndpoint_exposesHttpMetrics`: `/api/incidents` 호출 후 `GET /actuator/prometheus` → 200이고 본문에 `http_server_requests_seconds`가 포함.
- [ ] **Step 2: 실행해 FAIL 확인 → 의존성과 노출 설정을 추가해 PASS 확인.**
- [ ] **Step 3: Prometheus·Grafana를 compose에 추가하고** 데이터소스와 대시보드를 provisioning 파일로 자동 등록한다.
- [ ] **Step 4: 검증.** `docker compose up -d` 후 `curl`로 `/api/incidents`를 반복 호출 → `localhost:3000`의 5개 패널에 값이 나타남. 존재하지 않는 id를 반복 호출해 4xx가 보이는지도 확인.
- [ ] **Step 5: 서버에도 배포해 같은 화면을 확인한 뒤** destroy와 잔여 리소스 `[]` 확인. Commit & push.

### Task 8: 장애 재현과 문서화 (9주차)

**Files:**
- Create: `docs/incidents/TEMPLATE.md`, `docs/incidents/2026-<월>-<일>-db-down.md`, `...-oom-restart.md`, `...-bad-db-password.md` (3건, 일정이 밀리면 2건)

**Interfaces:**
- Consumes: Task 7의 Grafana 대시보드
- Produces: 각 문서는 `## 증상`, `## 감지`, `## 원인`, `## 해결`, `## 재발 방지` 5개 섹션

- [ ] **Step 1: `TEMPLATE.md`를 위 5개 섹션으로 만든다.**
- [ ] **Step 2: 장애 1 — DB 중단.** `docker compose stop db` → 앱의 5xx와 health 상태 변화를 Grafana에서 확인 → `start db`로 복구. 문서화.
- [ ] **Step 3: 장애 2 — 메모리 초과.** compose의 `app`에 `mem_limit`을 아주 낮게 줘서 재시작이 반복되는 상황을 만들고 `docker inspect`로 OOMKilled 확인 → 한도 조정으로 해결. 문서화.
- [ ] **Step 4: 장애 3 — 잘못된 DB 비밀번호.** `.env`를 틀리게 바꾸고 재배포 → 기동 실패 로그 확인 → 복구. 문서화.
- [ ] **Step 5: 검증.** `grep -c '^## ' docs/incidents/2026-*.md` 가 각 파일마다 `5`. 문서에 실제 비밀번호·IP가 없는지 눈으로 확인. Commit & push.

### Task 9: 정리와 지원 준비 (10주차)

**Files:**
- Modify: `README.md`
- Create: `docs/interview-notes.md`

**Interfaces:**
- Consumes: 앞선 모든 Task의 산출물

- [ ] **Step 1: `README.md`** — 소개, Mermaid 구조도 1장, 실행 방법, 그리고 읽는 순서 2가지(인프라/DevOps용: 배포 자동화 → 모니터링 → 장애 문서 → Atlas / 백엔드용: Atlas 코드·설계 → 테스트 → 배포 자동화).
- [ ] **Step 2: 처음부터 따라 하기 검증.** 새 폴더에 clone 후 README대로 `cp .env.example .env && docker compose up -d --build` → `/actuator/health`가 `UP`.
- [ ] **Step 3: 비밀 정보 검사.** `git log -p | grep -iE 'AKIA|aws_secret|password=' | head` 에 실제 값이 없어야 한다 (예시·가짜 값은 제외). 나오면 즉시 해당 키를 폐기·교체한다.
- [ ] **Step 4: `docs/interview-notes.md`** — 구성요소(Docker, CI/CD, Terraform, 모니터링, 장애 3건)마다 "왜 이렇게 했나, 대안은 무엇이었나, 직접 겪은 문제"를 3줄씩 쓰고 소리 내어 설명해 본다.
- [ ] **Step 5: Commit & push 후 지원을 시작한다.** 확장 과제(쿠버네티스 등)는 `README.md`의 "다음 계획" 섹션에 한 줄씩 적는다.
