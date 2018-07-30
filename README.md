![las2peer](https://rwth-acis.github.io/las2peer/logo/vector/las2peer-logo.svg)

# las2peer-Social-Bot-Manager-Service
This is the core service of the Social Bot Framework. This [las2peer](https://github.com/rwth-acis/las2peer) service can read bot models (created with [SyncMeta](https://github.com/rwth-acis/syncmeta)) and respond to RESTful service methods. 
The answer can either be another RESTful service call or a message to a Slack channel.
Answers are generated from modeled rules. TensorFlow services are used for generative content. At the moment there are two different ones:
- [las2peer-TensorFlow-TextToText-Service](https://github.com/rwth-acis/las2peer-TensorFlow-TextToText-Service)
- [las2peer-TensorFlow-Classifier-Service](https://github.com/rwth-acis/las2peer-TensorFlow-Classifier-Service)

This service requires the [MobSOS Data-Processing](https://github.com/rwth-acis/mobsos-data-processing) and the [MobSOS Success-Modeling](https://github.com/rwth-acis/mobsos-success-modeling) service and has to be started in monitoring mode. 

Build
--------
Execute the following command on your shell:

```shell
ant all 
```

Start
--------

To start the data-processing service, use one of the available start scripts:

Windows:

```shell
bin/start_network.bat
```

Unix/Mac:
```shell
bin/start_network.sh
```
