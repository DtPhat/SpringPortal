FROM maven:3.9.6-eclipse-temurin-21 AS build
LABEL authors="EC2"
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-alpine
WORKDIR /app
COPY --from=build /app/target/uniportal-0.0.1-SNAPSHOT.jar ./uniportal.jar
EXPOSE 8080
CMD ["java", "-jar", "uniportal.jar"]
