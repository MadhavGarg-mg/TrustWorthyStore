# Stage 1: Build the application using Maven with JDK 17
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app
# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source code and build the JAR (skipping tests for speed)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image using Eclipse Temurin (JDK 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the built JAR from the build stage; adjust the jar name if necessary
COPY --from=build /app/target/appstore-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
