FROM openjdk:14-jdk-alpine
ENV LAS2PEER_PORT=9011
ENV DATABASE_NAME=SBF
ENV DATABASE_HOST=mobsos-mysql.mobsos
ENV DATABASE_PORT=3306
ENV DATABASE_USER=root
ENV DATABASE_PASSWORD=root
RUN apk add --update bash mysql-client apache-ant tzdata curl && rm -f /var/cache/apk/*
ENV TZ=Europe/Berlin
RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer
COPY --chown=las2peer:las2peer . /src
WORKDIR /src
RUN chmod -R a+rwx /src
RUN chmod +x /src/docker-entrypoint.sh
# run the rest as unprivileged user
USER las2peer
RUN ant jar startscripts
RUN dos2unix /src/docker-entrypoint.sh
RUN dos2unix /src/etc/ant_configuration/service.properties
RUN dos2unix /src/etc/i5.las2peer.services.socialBotManagerService.SocialBotManagerService.properties.sample
RUN dos2unix /src/etc/i5.las2peer.services.socialBotManagerService.SocialBotManagerService.properties
EXPOSE $LAS2PEER_PORT
ENTRYPOINT ["/src/docker-entrypoint.sh"]
