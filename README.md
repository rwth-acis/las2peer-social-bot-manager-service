<p align="center">
  <img src="https://raw.githubusercontent.com/rwth-acis/Social-Bot-Framework/master/Logos/sbf-logo.svg" width=400px/>
</p>

<p align="center">
    <a href="https://github.com/rwth-acis/Social-Bot-Framework/blob/master/LICENSE">
        <img alt="GitHub" src="https://img.shields.io/github/license/rwth-acis/Social-Bot-Framework.svg?color=blue">
    </a>
    <a href="https://github.com/rwth-acis/Social-Bot-Framework/releases">
        <img alt="GitHub release" src="https://img.shields.io/github/release/rwth-acis/Social-Bot-Framework.svg">
    </a>
</p>

# 🤖 Social Bot Framework - Bot Manager Service

The Social Bot Framework is a versatile platform for developing and deploying bots for various communication channels, including Slack, Rocket.Chat. Telegram, and others.
The framework allows you to create bot models using [SyncMeta](https://github.com/rwth-acis/syncmeta), a web-based modeling tool.
The bot models can then be imported into the framework and used to respond to RESTful service methods.

This repository contains the Bot-Manager-Service, which is the core service of the Social Bot Framework. The Bot-Manager-Service is a [las2peer](https://github.com/rwth-acis/las2peer) service that can read bot models and respond to RESTful service methods. The response can be another RESTful service call or a message to a communication channel.

The Bot-Manager-Service requires the [MobSOS Data-Processing](https://github.com/rwth-acis/mobsos-data-processing) and the [MobSOS Success-Modeling](https://github.com/rwth-acis/mobsos-success-modeling) services and has to be started in monitoring mode.

## 🚀 Installation

To install the Bot-Manager-Service, you first need to install and set up [las2peer](https://github.com/rwth-acis/las2peer). 
Once you have done that, you can clone this repository and build the service.

### Build

You can build the service using the following command:

```shell
gradlew build
```

### Start

To start the Bot-Manager-Service, use the following command:

```shell
./start_network.bat
```

On Unix/Mac systems, use the following command instead:

```shell
./start_network.sh
```

## Update block messages

Set the response of the las2peer service (a JsonObject) to

```json
{
  "blocks": "the content of the new block message",
  "updateBlock": true,
  "ts": "the time when the need to be updated blocks have been sent"
}
```

## 🐳 How to run using Docker

First, build the image:

```bash
docker build -t social-bot-manager . 
```

Then you can run the image like this:

```bash
docker run -e DATABASE_HOST=host -e DATABASE_NAME=LAS2PEERMON -e DATABASE_USER=myuser -e DATABASE_PASSWORD=mypasswd -p 8080:8080 -p 9011:9011 social-bot-manager
```

Replace *myuser* and *mypasswd* with the username and password of a MySQL user with access to a database named *LAS2PEERMON*.
Note that you might need to setup your database with the tables found in [SBF.sql](https://github.com/rwth-acis/las2peer-social-bot-manager-service/blob/master/SBF.sql)
By default the database host is *mysql* and the port is *3306*.
The REST-API will be available via *http://localhost:8080/SBFManager* and the las2peer node is available via port 9011.

To customize your setup you can set further environment variables.

### Node Launcher Variables

Set [las2peer node launcher options](https://github.com/rwth-acis/las2peer-Template-Project/wiki/L2pNodeLauncher-Commands#at-start-up) with these variables.
The las2peer port is fixed at *9011*.

| Variable | Default | Description |
|----------|---------|-------------|
| BOOTSTRAP | unset | Set the --bootstrap option to bootrap with existing nodes. The container will wait for any bootstrap node to be available before continuing. |
| SERVICE_PASSPHRASE | processing | Set the second argument in *startService('<service@version>', '<SERVICE_PASSPHRASE>')*. |
| SERVICE_EXTRA_ARGS | unset | Set additional launcher arguments. Example: ```--observer``` to enable monitoring. |

### Service Variables

See [database](#Database) for a description of the settings.

| Variable | Default | Description |
|----------|---------|-------------|
| DATABASE_USER | *mandatory* ||
| DATABASE_PASSWORD | *mandatory* ||
| DATABASE_HOST | mysql | |
| DATABASE_PORT | 3306 | |
| ADDRESS | / |Webconnector address. Needed for auto-restart functionality |
| RESTARTERBOTNAME | / | Name of restarterBot agent. Needed for auto-restart functionality |
| RESTARTERBOTPW | / | Password for restarterBot agent. Needed for auto-restart functionality |

### Web Connector Variables

Set [WebConnector properties](https://github.com/rwth-acis/las2peer-Template-Project/wiki/WebConnector-Configuration) with these variables.
*httpPort* and *httpsPort* are fixed at *8080* and *8443*.

| Variable | Default |
|----------|---------|
| START_HTTP | TRUE |
| START_HTTPS | FALSE |
| SSL_KEYSTORE | "" |
| SSL_KEY_PASSWORD | "" |
| CROSS_ORIGIN_RESOURCE_DOMAIN | * |
| CROSS_ORIGIN_RESOURCE_MAX_AGE | 60 |
| ENABLE_CROSS_ORIGIN_RESOURCE_SHARING | TRUE |
| OIDC_PROVIDERS | https://api.learning-layers.eu/o/oauth2,https://accounts.google.com |

### Other Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DEBUG  | unset | Set to any value to get verbose output in the container entrypoint script. |
