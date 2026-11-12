# 학습 일지

주차 끝마다 배운 것과 막혔던 것을 3~5줄로 적습니다. 이 기록이 일정 조정의 근거입니다.

## 1주차 (2026-11-11 시작)

- 배운 것: `.gitignore`에 `.env`를 넣으면 파일을 만들어도 `git status`에 안 나타난다. `git check-ignore -v .env`로 어떤 규칙이 막는지 확인할 수 있다.
- 배운 것: WSL에 배포판이 여러 개(Ubuntu, docker-desktop)면 `wsl -d Ubuntu`로 지정해야 한다. 기본 배포판이 docker-desktop이면 bash가 없다.
- 막혔던 것: 커밋 메시지에 줄바꿈과 따옴표를 섞어 한 줄로 넘겼더니 메시지가 `chore:`에서 잘렸다. 푸시 전이라 `git commit --amend`로 고쳤다. 긴 메시지는 파일로 넣는 편이 안전하다(`-F`).
- 막혔던 것: (직접 적기)

## 2주차 (Atlas 서비스)

- 결정: 플랜은 Spring Boot 3이었지만 Initializr가 4.x만 제공해서 4.1.2(Java 21)로 진행했다. 테스트의 `@AutoConfigureMockMvc` 패키지가 `org.springframework.boot.webmvc.test.autoconfigure`로 바뀐 점이 달랐다.
- 막혔던 것: Initializr zip을 저장소에 풀자 `.gitignore`와 `README.md`가 덮어써졌다. `git checkout`으로 되돌리고, 임시 폴더에 푼 뒤 필요한 파일만 복사했다.
- 확인: 테스트 10개 통과, 로컬 실행에서 등록 201, 키워드 검색 1건, 없는 id 404.

## 3주차 (Docker)

- 배운 것: 멀티스테이지 Dockerfile은 빌드(JDK)와 실행(JRE)을 나눠 실행 이미지를 작게 만든다. 컨테이너는 root가 아닌 일반 사용자로 띄웠다.
- 배운 것: compose의 `depends_on: condition: service_healthy`는 DB가 접속을 받을 수 있을 때까지 앱 시작을 미룬다. 앱에서 DB 주소는 `localhost`가 아니라 서비스 이름 `db`다.
- 막혔던 것: Docker Desktop의 WSL 연동이 꺼져 있어 Ubuntu에서 `docker`가 없었다. Settings → Resources → WSL integration에서 Ubuntu를 켰다.
- 확인: `docker compose up -d --build` 후 health `UP`, 1건 등록 → `down`/`up` 후에도 조회됨(볼륨 `db-data`).

## 4~5주차 (CI)

- 배운 것: GitHub Actions의 `needs: test`로 테스트가 성공해야만 이미지 빌드가 돈다. `if:`로 main에 push할 때만 이미지를 올린다(PR이나 다른 브랜치는 테스트만).
- 배운 것: GHCR 로그인에는 따로 만든 토큰이 아니라 Actions가 주는 `GITHUB_TOKEN`을 쓰고, 잡에 `packages: write` 권한을 준다. 이미지 이름은 소문자여야 해서 `${GITHUB_REPOSITORY,,}`로 바꿨다.
- 경험: 테스트 하나를 일부러 깨뜨린 브랜치를 올려 빨간불(AssertionError at IncidentControllerTest.java:45)을 보고, revert 후 초록불로 돌아오는 것을 확인했다.
- 확인: main push → test, build-and-push 모두 성공, 이미지 `ghcr.io/hanshaug/infra-portfolio:<sha>`와 `:latest`가 올라감.
