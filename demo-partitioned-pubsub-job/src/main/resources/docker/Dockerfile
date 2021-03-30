#FROM openjdk:8-jdk-alpine
FROM adoptopenjdk/openjdk11:alpine
COPY demo-partitioned-pubsub-job-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
