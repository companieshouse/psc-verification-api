# psc-verification-api

The Persons with Significant Control (PSC) Verification API is a Spring Boot REST API that forms part of the Identification Verification (IDV) service. It is responsible for handling and processing PSC Individual verification requests.

API users, including [psc-verification-web](https://github.com/companieshouse/psc-verification-web), interact with the **psc-verification-api** by sending HTTP requests containing JSON data to the service endpoints. You can find the service endpoints and their expected request and response models [here](https://companieshouse.atlassian.net/wiki/spaces/IDV/pages/4339236904/PSC+Verification+Directions+and+Extensions#Endpoints-in-psc-verification-filing-api).

The service integrates with several internal and external systems, including [transactions-api](https://github.com/companieshouse/transactions.api.ch.gov.uk). Through the transactions API, it connects to [chips-filing-consumer](https://github.com/companieshouse/chips-filing-consumer) and [chs-notification-api](https://github.com/companieshouse/chs-notification-api) to notify applicants and internal users if/when an application has been submitted, converted, accepted, or rejected. The [api-enumerations](https://github.com/companieshouse/api-enumerations) define the **psc-verification-api** data types.

## Requirements

To build **psc-verification-api**, you will need:
* [Git](https://git-scm.com/downloads)
* [Java 21](https://www.oracle.com/uk/java/technologies/downloads/#java21)
* [Maven](https://maven.apache.org/download.cgi)
* [MongoDB](https://www.mongodb.com/) 
* [docker-chs-development](https://github.com/companieshouse/docker-chs-development)
* Internal Companies House core services

You will also need a REST client (e.g. Postman or cURL) if you wish to interact with the **psc-verification-api** service endpoints.

## Building

From the command line, in the project root, run:

```shell
make clean build
```

This will clean, build, and test the code.

## Running Locally using Docker

1. Clone [docker-chs-development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.
2. Enable the **psc-verification-api** services:

```shell
chs-dev modules enable psc-verification
chs-dev services enable psc-verification-api
```

3. Run `chs-dev up` and wait for all services to start.
4. Send a GET request using your REST client to `/persons-with-significant-control-verification/healthcheck`. The response should be `200 OK` with `status=UP`.

### To make local changes

Development mode is available for this service in [docker-chs-development](https://github.com/companieshouse/docker-chs-development).

```shell
chs-dev development enable psc-verification-api
```

This will clone **psc-verification-api** into the `docker-chs-development/repositories` directory. Any changes to the code or resources will automatically trigger a rebuild and relaunch.

# Documentation

See the following Confluence pages:

* [High-level Design (HLD)](https://companieshouse.atlassian.net/wiki/x/roAxBAE)
* [Low-level Design (LLD)](https://companieshouse.atlassian.net/wiki/x/poAvBAE)

# Configuration

System properties for **psc-verification-api** are defined in `application.properties`. These are normally configured per environment. For example values, see the [relevant docker-compose.yaml](https://github.com/companieshouse/docker-chs-development/blob/master/services/modules/psc-verification/psc-verification-api.docker-compose.yaml) in docker-chs-development.

*If a variable appears in italics, it's optional.*

| Variable | Description |
|----------|-------------|
| ACCOUNT_COOKIE_DOMAIN | Domain for account-related cookies. |
| API_URL | URL for API calls. |
| CHS_INTERNAL_API_KEY | API key for internal CHS services. |
| COOKIE_DOMAIN | Domain for cookies. |
| COOKIE_NAME | Name of the cookie used for session management. |
| COOKIE_SECRET | Secret key used to sign and verify cookies. |
| COOKIE_SECURE_ONLY | Flag indicating whether cookies should only be sent over secure (HTTPS) connections. |
| *FEATURE_FLAG_TRANSACTIONS_CLOSABLE_250124* | Feature flag to enable or disable the ability to close transactions. |
| *HUMAN_LOG* | Flag to enable human-readable logging (0 or 1). |
| INTERNAL_API_URL | URL for internal API calls. |
| MANAGEMENT_ENDPOINT_HEALTH_ENABLED | Flag to enable or disable the health management endpoint. |
| MANAGEMENT_ENDPOINTS_WEB_BASE_PATH | Base path for web management endpoints. |
| MANAGEMENT_ENDPOINTS_WEB_PATH_MAPPING_HEALTH | Path mapping for the healthcheck endpoint. |
| MONGO_PSC_VERIFICATIONS_API_DB_NAME | Name of the MongoDB database used for storing PSC verification filings. |
| MONGODB_URL | Connection URL for the MongoDB instance. |
| *PLANNED_MAINTENANCE_START_TIME*[^1] | Start time for a planned maintenance period. |
| *PLANNED_MAINTENANCE_END_TIME*[^1] | End time for a planned maintenance period. |
| *PLANNED_MAINTENANCE_MESSAGE* | Message displayed during a planned maintenance period. |
| PSC_VERIFICATION_DESCRIPTION | Description of the PSC verification service. |
| *WEB_LOGGING_LEVEL* | Logging level for Spring Web. |

[^1]: When setting planned maintenance times, ensure both the start and end times are specified.

## Notes

Planned maintenance format: `d MMM yy HH:mm z|x` where:

- `d` is the day of the month (1-31).
- `MMM` is the 3-letter month abbreviation (case sensitive: e.g. `Nov` not `NOV`).
- `HH:mm` is the time in 24-hour format (e.g. `00:30`).
- `z` is the zone short name, e.g. `GMT`.
- `x` is the 2-digit zone offset from UTC, e.g. `+01` (= British Summer Time).

> [!CAUTION]
> - Zone short name `GMT` always denotes Greenwich Mean Time (UTC+00).
> - During daylight saving time:
>   - Use zone offset `+01` to specify British Summer Time (UTC+01).
>   - Do not use zone short name `BST`, as it denotes Bangladesh Standard Time (UTC+06).
