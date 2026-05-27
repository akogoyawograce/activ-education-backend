FROM eclipse-temurin:21-jre
WORKDIR /app

COPY target/*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
