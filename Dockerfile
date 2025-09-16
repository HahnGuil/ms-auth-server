# Use a base image with JDK 24
FROM eclipse-temurin:24-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build file and source code
COPY pom.xml ./
COPY src ./src

# Install Maven and build the application
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Expose the application port
EXPOSE 2300

# Run the application (as variáveis virão do ambiente do Kubernetes)
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "target/ms-auth-server-1.0.1.jar"]