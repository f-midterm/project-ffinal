# --- Build Stage ---
FROM openjdk:21-jdk-slim as builder

WORKDIR /app

# Copy Gradle related files FIRST
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle

# NOW grant execution permission to the Gradle wrapper
RUN chmod +x ./gradlew

# Copy source code to leverage Docker cache
COPY src ./src

# Build the application and create the executable jar
RUN ./gradlew bootJar --no-daemon

# --- Run Stage ---
# FROM openjdk:21-jre-slim
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the packaged jar from the builder stage
# Note: Gradle's output directory is build/libs
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

