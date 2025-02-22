# 1. Gradle과 JDK 이미지를 선택합니다
FROM gradle:8.12.1-jdk23 AS build

# 2. 작업 디렉토리 설정
WORKDIR /spring-boot

# 3. 프로젝트 소스 코드 복사
COPY . .

# 4. 클린 빌드를 실행합니다
RUN gradle clean build

# 5. 실제 실행을 위한 JDK 23 이미지 사용
FROM openjdk:23-jdk

# 6. 작업 디렉토리 설정
WORKDIR /spring-boot

# 7. 빌드된 JAR 파일을 복사합니다
COPY --from=build /spring-boot/build/libs/*SNAPSHOT.jar app.jar

# 8. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/spring-boot/app.jar"]
