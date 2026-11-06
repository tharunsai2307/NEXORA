# Multi-stage Dockerfile for Spring Boot Java 21 Backend
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven files
COPY pom.xml ./
COPY src ./src

# Build production JAR (skipping tests inside docker build, tests executed in CI/CD pipeline)
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for docker healthcheck
RUN apk add --no-cache curl

# Copy fat JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=20s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
