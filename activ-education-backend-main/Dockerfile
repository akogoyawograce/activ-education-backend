# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copie du pom.xml et téléchargement des dépendances (pour le cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copie du code source et compilation
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale légère pour l'exécution
FROM eclipse-temurin:21-jre
WORKDIR /app

# On récupère le JAR créé à l'étape précédente
COPY --from=build /app/target/*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
