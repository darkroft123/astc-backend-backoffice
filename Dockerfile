FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

# opcional pero más seguro en Docker
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests -Dquarkus.package.jar.type=uber-jar

FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20

WORKDIR /app

COPY --from=build /app/target/*-runner.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]