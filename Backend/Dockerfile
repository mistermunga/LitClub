FROM openjdk:25-ea-21-jdk-slim
LABEL authors="Ryan"

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY build/libs/*-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]