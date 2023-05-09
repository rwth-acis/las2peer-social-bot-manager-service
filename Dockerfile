# first stage: build using gradle 
FROM --platform=${BUILDPLATFORM:-amd64} gradle:7.5.0-jdk17 AS build

ENV GRADLE_OPTS="-Xmx2048m -Xms512m -Dorg.gradle.daemon=true -Dorg.gradle.parallel=true"

COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN chmod -R a+rwx /src
RUN chmod +x /src/docker-entrypoint.sh

RUN gradle clean
RUN gradle build --exclude-task test 

# second stage: create the docker image
FROM --platform=amd64 openjdk:17-jdk-alpine

ENV LAS2PEER_PORT=9011
ENV DATABASE_NAME=SBF
ENV DATABASE_HOST=mobsos-mysql.mobsos
ENV DATABASE_PORT=3306
ENV DATABASE_USER=root
ENV DATABASE_PASSWORD=root
ENV TZ=Europe/Berlin

RUN apk add --update bash mysql-client tzdata curl && rm -f /var/cache/apk/*

RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /app
WORKDIR /app
RUN chmod -R a+rwx /app
RUN chmod +x /app/docker-entrypoint.sh
# run the rest as unprivileged user
USER las2peer

COPY --from=build --chown=las2peer:las2peer /src/social-bot-manager/export /app/social-bot-manager/export/

RUN dos2unix /app/gradle.properties
RUN dos2unix /app/docker-entrypoint.sh

RUN dos2unix /app/etc/i5.las2peer.services.socialBotManagerService.SocialBotManagerService.properties.sample
RUN dos2unix /app/etc/i5.las2peer.services.socialBotManagerService.SocialBotManagerService.properties

EXPOSE $LAS2PEER_PORT
ENTRYPOINT ["/app/docker-entrypoint.sh"]
