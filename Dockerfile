FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN set -e; \
      echo "⏳ Compilando con Maven..."; \
      start=$(date +%s); \
      mvn clean package -DskipTests -Dquarkus.package.jar.type=uber-jar -Daether.syncContext.named.factory=noop -Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120; \
      end=$(date +%s); \
      echo "⏱️ La compilación tardó $((end - start)) segundos."

FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20
WORKDIR /app
COPY --from=builder /build/target/*-runner.jar app.jar
EXPOSE 8082
ENV TZ=UTC
ENV JAVA_OPTS="-Duser.timezone=UTC -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV S3_ENDPOINT=http://minio:9000
ENV S3_PUBLIC_ENDPOINT=http://localhost:9010
ENV S3_ACCESS_KEY=minioadmin
ENV S3_SECRET_KEY=minioadmin
ENV S3_BUCKET=backoffice
ENV S3_REGION=us-east-1
CMD ["java", "-jar", "app.jar"]
