# Purpose: Builds and runs the Spring Boot core backend service shell.
# Why it exists: Gives the backend an independently deployable container with actuator health before business implementation.
# Architecture fit: Supports the approved containerized Kubernetes deployment model for the core backend.
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY services/core-backend/pom.xml services/core-backend/pom.xml
COPY packages/java-shared/pom.xml packages/java-shared/pom.xml
COPY services/core-backend/src services/core-backend/src
COPY packages/java-shared packages/java-shared
COPY ml-platform/evaluation/heldout/evaluation-set-v1.0.0 /app/ml-platform/evaluation/heldout/evaluation-set-v1.0.0
RUN chmod +x mvnw && ./mvnw -B -pl services/core-backend -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/services/core-backend/target/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
