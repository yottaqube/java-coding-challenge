# Use Eclipse Temurin OpenJDK 17 as base image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY src ./src

# Install Maven and curl
RUN apt-get update && \
    apt-get install -y maven curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Build the application
RUN mvn clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "target/order-system-0.0.1-SNAPSHOT.jar"]