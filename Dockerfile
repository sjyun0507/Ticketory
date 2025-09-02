# 1단계: 빌드
FROM gradle:8.7.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test

# 2단계: 실행
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]