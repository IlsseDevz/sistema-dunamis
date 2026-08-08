# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -Dmaven.test.skip=true

COPY src ./src
RUN ./mvnw clean package -B -Dmaven.test.skip=true

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S dunamis && adduser -S dunamis -G dunamis

COPY --from=build /app/target/sistema-dunamis-*.jar app.jar

USER dunamis

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
