FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

COPY . .

# Запускаем тесты
RUN gradle test --no-daemon

# Полноценная сборка
RUN gradle build --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
