FROM  maven:3.9-eclipse-temurin-21 AS build
# All copying puts files in WORKDIR
WORKDIR /app
# Parent and child pom
COPY pom.xml pom.xml
COPY market-data-service/pom.xml market-data-service/pom.xml
# Build .jar from source code
RUN mvn -f pom.xml dependency:go-offline -B
COPY market-data-service/src market-data-service/src
RUN mvn -f market-data-service/pom.xml package -DskipTests -B

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /app/market-data-service/target/market-data-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
# Documentation. Does not expose ports at runtime (requires docker run -p "<host>:<container_internal>." ) but used by docker inspect
EXPOSE 8080


