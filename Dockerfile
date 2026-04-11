FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -B -q -Dmaven.repo.local=/root/.m2/repository dependency:go-offline

COPY src ./src
RUN mvn -B -q -Dmaven.repo.local=/root/.m2/repository package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/document-management-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
