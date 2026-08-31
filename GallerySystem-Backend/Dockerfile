
# Build stage
FROM eclipse-temurin:8-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build configuration
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Copy application source
COPY src src

# Build the Spring Boot application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:8-jre

WORKDIR /app

# Copy the generated Spring Boot JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Spring Boot uses the PORT environment variable when provided
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
