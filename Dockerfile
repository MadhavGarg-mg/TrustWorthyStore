# Stage 1: Build the application using Maven with JDK 17
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the entire project — not just pom.xml + src
COPY . .

# Build the app (skip tests for now)
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the freshly built JAR
COPY --from=build /app/target/appstore-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
