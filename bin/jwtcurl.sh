#!/usr/bin/env bash

##
#
# Usage
#
# jwtcurl [-u username:password] [-b BASE_URL] "[-X GET|POST|PUT|DELETE] BO_API_URL"
#
# Options:
#   -v: verbose output
#   -u: basic auth parameter in username:password format
#   -b: base url of jenkins without trailing slash. e.g. http://localhost:8080/jenkins or https://blueocean.io
#
#  Note: You need to enclose last argument in double quotes if you are passing arguments to curl.
#
# Examples:
#
# Anonymous user:
#
# jwtcurl http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/p1/
#
# User with credentials:
#
# jwtcurl -u admin:admin http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/p1/
#
# Use base url other than http://localhost:8080/jenkins
#
# jwtcurl -u admin:admin -b https://myjenkinshost http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/p1/
#
# Author: Vivek Pandey
#
##
if [ $# -eq 0 ]
  then
    echo "Usage: jwtcurl [-v] [-u username:password] [-b BASE_URL] \"-X [GET|POST|PUT|DELETE] BO_API_URL\""
    exit 1;
fi

while [[ $# -gt 1 ]]
do
key="$1"

case $key in
    -u)
    CREDENTIAL="-u $2"
    shift
    ;;
    -b)
    BASE_URL="$2"
    shift
    ;;
    -v)
    VERBOSE="$2"
    shift
    ;;
    *)
    # unknown option
    ;;
esac
shift
done

if [ ! -z "$VERBOSE" ]; then
    SETX="set -x"
    CURL_VERBOSE="-v"
fi

if [ -z "${BASE_URL}" ]; then
    BASE_URL=http://localhost:8080/jenkins
fi

${SETX}

TOKEN=$(curl ${CURL_VERBOSE} -s -X GET ${CREDENTIAL} -I ${BASE_URL}/jwt-auth/token | awk 'BEGIN {FS=": "}/^X-BLUEOCEAN-JWT/{print $2}'|sed $'s/\r//')

if [ -z "${TOKEN}" ]; then
    echo "Failed to get JWT token"
    echo $?
    exit 1
fi

curl ${CURL_VERBOSE} -H "Authorization: Bearer ${TOKEN}"  $@

