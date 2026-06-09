# 1단계: 빌드 스테이지 (Gradle 빌드 도구와 JDK 17 포함 이미지 사용)
FROM gradle:8-jdk17 AS build
WORKDIR /app

# 빌드 설정 파일 및 소스코드 복사
COPY build.gradle settings.gradle ./
COPY src ./src

# Gradle을 사용하여 JAR 패키지 빌드 (테스트는 스킵하여 시간 단축)
RUN gradle build -x test --no-daemon

# 2단계: 실행 스테이지 (가벼운 JRE 17 이미지 사용)
FROM eclipse-temurin:17-jre
WORKDIR /app

# 1단계 빌드 결과물 중 JAR 파일만 실행 환경으로 가져옴
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# 백엔드가 사용하는 8080 포트 오픈
EXPOSE 8080

# 컨테이너 실행 시 Spring Boot 기동
ENTRYPOINT ["java", "-jar", "app.jar"]
