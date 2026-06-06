FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the JAR file
COPY target/metaTry-0.0.1-SNAPSHOT.jar app.jar

# Create a non-root user to run the app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]