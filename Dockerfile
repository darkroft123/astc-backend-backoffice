FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 echo "⏳ Compilando con Maven..."; \
      start=$(date +%s); \
      mvn -o clean package -DskipTests -Dquarkus.package.jar.type=uber-jar || mvn clean package -DskipTests -Dquarkus.package.jar.type=uber-jar; \
      end=$(date +%s); \
      echo "⏱️ La compilación tardó $((end - start)) segundos."

FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20
WORKDIR /app
COPY --from=builder /build/target/*-runner.jar app.jar
EXPOSE 8082
ENV TZ=UTC
ENV JAVA_OPTS="-Duser.timezone=UTC -XX:+UseContainerSupport"
ENTRYPOINT ["java", "-jar", "app.jar"]
