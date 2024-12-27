# Stage 1: Build the Spring Boot application
FROM ghcr.io/graalvm/native-image-community:21.0.0 AS build

COPY --from=maven:3.9.9-eclipse-temurin-17 /usr/share/maven /usr/share/maven

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the Spring Boot application
RUN /usr/share/maven/bin/mvn -Pnative native:compile

# Stage 2: Run the spring native application.
# Just use something small with glibc and curl. ubuntu:22.04 ships no curl, rockylinux:9 does.
# This avoids apt-get update/install, which leads to flakiness on mirror upgrades.
FROM rockylinux:9

# Set the working directory
WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/target/spring-native spring-native

# Expose the application port
EXPOSE 8080

RUN chmod +x spring-native

# Command to run the application with the Datadog Java Agent
CMD ["./spring-native"]
