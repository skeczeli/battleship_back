# Etapa 1: Build con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final solo con JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# DEBUG: Mostrar si la variable se setea (NO aparece en Render a esta altura)
RUN echo "🔐 SPRING_DATASOURCE_PASSWORD at build time: $SPRING_DATASOURCE_PASSWORD"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
