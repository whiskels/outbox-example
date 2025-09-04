FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY gradlew settings.gradle* build.gradle* gradle ./
COPY gradle/wrapper gradle/wrapper
COPY order-service/build.gradle order-service/build.gradle
COPY order-service/src order-service/src
RUN ./gradlew :order-service:bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/order-service/build/libs/*.jar order-service.jar
EXPOSE ${SERVICE_PORT}
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar order-service.jar"]