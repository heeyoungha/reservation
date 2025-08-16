# Multi-stage build for Flight Search Service

# Build stage
FROM eclipse-temurin:17-jdk AS build

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle ./
COPY settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (this layer will be cached)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 