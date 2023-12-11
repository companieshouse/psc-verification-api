#!/bin/bash
#
# Start script for psc-verification-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" "psc-verification-api.jar"