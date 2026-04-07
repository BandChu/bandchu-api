FROM amazoncorretto:21

ARG JAR_FILE=build/libs/*.jar

RUN mkdir -p /app/logs

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]