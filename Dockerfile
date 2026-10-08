# ---- Build stage ----
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
# Gradle files first: dependency layer stays cached unless deps change
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon -q || true
# Source last: code changes only rebuild this layer
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---- Runtime stage (LTS major pinned, JRE only) ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
