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
