# psc-verification-api

The People with Significant Control (PSC) Verification API is a Spring Boot REST API which forms part of the Identification Verification (IDV) service and is responsible for handling and processing PSC Individual verification requests.

API users, including <code>[psc-verification-web](https://github.com/companieshouse/psc-verification-web)</code>, interact with the `psc-verification-api` by sending HTTP requests containing JSON data to the service endpoints. The service endpoints available in the `psc-verification-api` as well as their expected request and response models are outlined in the [Swagger specification file](spec/swagger.json).

The service integrates with a number of internal and external systems. This includes <code>[chips-filing-consumer](https://github.com/companieshouse/chips-filing-consumer)</code> and <code>[chs-notification-api](https://github.com/companieshouse/chs-notification-api)</code> (via Kafka) to notify applicants and internal users if/when an application has been submitted, converted, accepted or rejected. The <code>[api-enumerations](https://github.com/companieshouse/api-enumerations)</code> will define the `psc-verification-api` data types. 

Requirements
------------

To build the `psc-verification-api`, you will need:
* [Git](https://git-scm.com/downloads)
* [Java 21](https://www.oracle.com/uk/java/technologies/downloads/#java21)
* [Maven](https://maven.apache.org/download.cgi)
* [MongoDB](https://www.mongodb.com/) 
* [Apache Kafka](https://kafka.apache.org/)
* Internal Companies House core services

You will also need a REST client (e.g. Postman or cURL) if you wish to interact with the `psc-verification-api` service endpoints.

## Building and Running Locally

1. From the command line, in the same folder as the Makefile run `make clean build`
1. Configure project environment variables where necessary (see below).
1. Ensure dependent Companies House services are running within the Companies House developer environment
1. Start the service in the CHS developer environment
1. Send a GET request using your REST client to `persons-with-significant-control-verification/healthcheck`. The response should be `200 OK` with `status=UP`.
1. A MongoDB instance named _TBC_ and the following collections are required: _TBC_



Configuration
-------------
System properties for the `psc-verification-api` are defined in `application.properties`. These are normally configured per environment.

Variable| Description                                                                           | Example                                    |
-------------------|---------------------------------------------------------------------------------------|--------------------------------------------|
_TBC_| The name of the collection responsible for storing PSC verification filings           | verifications                              |
MONGODB_URL| The URL of the MongoDB instance where documents and application data should be stored | mongodb://mongohost:27017/verifications    |
 PLANNED_MAINTENANCE_START_TIME               | Datetime for start of out-of-service period                                           | 14 July 24 00:30 +01 |optional; requires End time (see below)
 PLANNED_MAINTENANCE_END_TIME                 | Datetime for end of out-of-service period                                             | 2 Dec 23 02:30 GMT|optional; requires Start time (see above)
 PLANNED_MAINTENANCE_MESSAGE                  | Message output during the out-of-service period                                       | Service is undergoing planned maintenance  |optional; default value *UNAVAILABLE - PLANNED MAINTENANCE*

### Notes

Planned maintenance format: `d MMM yy HH:mm z|x` where
- `MMM` is the 3-letter month abbrev. (case sensitive: e.g. `Nov` not `NOV`)
- `z` is the zone short name e.g. `GMT`
- `x` is the 2-digit zone offset from UTC e.g. `+01`  (= British Summer Time)

> **CAUTION**: Use zone offset *+01* for Daylight Saving Time (British Summer Time). Zone short name *BST* denotes Bangladesh Standard Time (UTC+06) not British Summer Time (UTC+01).


## Building the docker image

    mvn -s settings.xml compile jib:dockerBuild -Dimage=416670754337.dkr.ecr.eu-west-2.amazonaws.com/psc-verification-api:latest

## Running Locally using Docker

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.

1. Enable the `psc-verification-api` services

1. Run `tilt up` and wait for all services to start

### To make local changes

Development mode is available for this service in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable psc-verification-api

This will clone the <code>psc-verification-api</code> into the repositories folder inside <code>docker-chs-dev</code>. Any changes to the code, or resources will automatically trigger a rebuild and relaunch.

## Validation of filing data
Validation is carried out using:
- Interceptors
- Validation messages
