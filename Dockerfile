# 멀티스테이지 빌드를 사용한 Spring Boot Dockerfile
FROM eclipse-temurin:17-jdk AS build

# Gradle 설치
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip && \
    mv gradle-8.5 /opt/gradle && \
    rm gradle-8.5-bin.zip && \
    apt-get clean

ENV PATH="/opt/gradle/bin:${PATH}"

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 파일들 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew

# 의존성 다운로드 (캐시 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre

# 애플리케이션 사용자 생성
RUN addgroup --system spring && adduser --system spring --ingroup spring

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown spring:spring app.jar

# 사용자 변경
USER spring:spring

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 