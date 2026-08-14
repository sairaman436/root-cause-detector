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
COPY artifacts/evaluation artifacts/evaluation
# Fresh Docker builders can experience transient DNS failures while resolving
# Maven model POMs, before Wagon's artifact retry handler is active. Retry the
# same verified Maven invocation rather than changing dependencies or mirrors.
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && \
    attempt=1; \
    while [ "$attempt" -le 3 ]; do \
      if ./mvnw -B \
        -Dmaven.wagon.http.retryHandler.count=5 \
        -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
        -Dmaven.wagon.http.readTimeout=300000 \
        -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
        -pl services/core-backend -am -DskipTests package; then \
        exit 0; \
      fi; \
      echo "Maven build attempt $attempt failed; retrying after transient repository failure"; \
      attempt=$((attempt + 1)); \
      sleep 5; \
    done; \
    exit 1

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/services/core-backend/target/app.jar /app/app.jar
COPY --from=build /app/artifacts/evaluation /app/artifacts/evaluation
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
