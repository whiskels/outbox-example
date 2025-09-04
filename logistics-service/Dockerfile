FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY gradlew settings.gradle* build.gradle* gradle ./
COPY gradle/wrapper gradle/wrapper
COPY logistics-service/build.gradle logistics-service/build.gradle
COPY logistics-service/src logistics-service/src
RUN ./gradlew :logistics-service:bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/logistics-service/build/libs/*.jar logistics-service.jar
EXPOSE ${SERVICE_PORT}
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar logistics-service.jar"]