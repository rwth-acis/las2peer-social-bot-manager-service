FROM gradle:jdk17-alpine AS builder

WORKDIR /social-bot-manager

COPY --chown=gradle:gradle social-bot-manager/build.gradle settings.gradle /social-bot-manager/
COPY --chown=gradle:gradle social-bot-manager/src /social-bot-manager/src
COPY --chown=gradle:gradle gradle.properties /social-bot-manager/gradle.properties

RUN gradle --no-daemon build -x test

# Use a Java base image
FROM openjdk:17-jdk-buster

RUN apt-get update && apt-get install -y bash tzdata curl && rm -rf /var/lib/apt/lists/*

# Set the working directory 
COPY . /src
WORKDIR /src

# Copy the Spring Boot application JAR file into the Docker image
COPY --from=builder /social-bot-manager/build/libs/*.jar /src/social-bot-manager-4.0.0.jar

# Set environment variables
ENV WEBCONNECTOR_URL=http://localhost:8080
ENV SERVER_PORT=8080
ENV ISSUER_URI=https://auth.las2peer.org/auth/realms/main 
ENV SET_URI=https://auth.las2peer.org/auth/realms/main/protocol/openid-connect/certs
ENV SPRING_DATASOURCE_URL=
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update
ENV SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/
ENV SPRING_DATA_MONGODB_DATABASE=
ENV XAPI_URL=
ENV XAPI_HOMEPAGE=
ENV TZ=Europe/Berlin 

# Expose the port that the Spring Boot application is listening on
EXPOSE 8080

# Set the entry point to run the docker-entrypoint.sh script
ENTRYPOINT ["java","-jar","/src/social-bot-manager-4.0.0.jar", "--spring.security.oauth2.resourceserver.jwt.issuer-uri=${ISSUER_URI}", "--spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${SET_URI}"]

