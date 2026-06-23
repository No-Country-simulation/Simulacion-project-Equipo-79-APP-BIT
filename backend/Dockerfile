FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 7860

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=7860"]