# ---- Build stage ----
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

# ---- Runtime stage ----
FROM azul/zulu-openjdk:21-latest
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
