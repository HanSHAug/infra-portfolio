# 1단계: 빌드 (JDK 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# 2단계: 실행 (JRE 21, 일반 사용자)
FROM eclipse-temurin:21-jre
RUN useradd --system --create-home atlas
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
USER atlas
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
