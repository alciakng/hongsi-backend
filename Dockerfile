FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app/app.jar
WORKDIR /app
EXPOSE 5000
ENTRYPOINT ["java","-jar","app.jar"]