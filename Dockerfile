# Multi-stage Dockerfile for Spring Boot Application

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
