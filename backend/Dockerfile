# 다단계 빌드 — JDK 17로 빌드, JRE 21로 슬림 실행
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle 캐시 효율을 위해 wrapper와 build 스크립트 먼저 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version

# 소스 복사 후 jar 빌드 (테스트는 CI에서. 배포 빌드는 스킵)
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# 실행 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너의 PORT는 플랫폼이 주입 (Render/Fly/Railway 모두 PORT 환경변수)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
