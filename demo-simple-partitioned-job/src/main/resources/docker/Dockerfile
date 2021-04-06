#FROM openjdk:8-jdk-alpine
FROM adoptopenjdk/openjdk11:alpine
COPY demo-simple-partitioned-job-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
