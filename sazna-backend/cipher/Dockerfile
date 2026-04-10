# Use OpenJDK 25 as the base image
FROM openjdk:25-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file (we'll build it in the next step)
COPY sazna-backend/cipher/build/libs/cipher-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8081
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]