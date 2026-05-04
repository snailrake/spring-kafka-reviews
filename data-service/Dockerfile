FROM gradle:8.7-jdk17 AS builder

WORKDIR /workspace
COPY . .

RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /workspace/build/libs/data-service-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]