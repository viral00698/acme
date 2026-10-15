# Stage 1: Build Maven Application
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Distroless / Temurin JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install curl for health check and tzdata for timezone accuracy
RUN apk add --no-cache curl tzdata

# Create dedicated non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR
COPY --from=build /app/target/acme-salary-management-1.0.0.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=35s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
