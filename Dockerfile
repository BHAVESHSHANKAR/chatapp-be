# Use an official OpenJDK 17 image as the base
     FROM openjdk:17-jdk-slim

     # Set the working directory inside the container
     WORKDIR /app

     # Copy the .env file to the container
     COPY .env .env

     # Copy the built JAR file (assumes Maven builds the JAR in target/)
     COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

     # Expose the application port (8080 as per application.properties)
     EXPOSE 8080

     # Command to run the application
     # Uses java -jar to run the Spring Boot application
     CMD ["java", "-jar", "app.jar"]