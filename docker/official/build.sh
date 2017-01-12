#!/bin/bash

set -e
set -x

HERE=$(dirname $0)
cd $HERE

BLUEOCEAN_VERSION=$(git for-each-ref --sort=-taggerdate refs/tags  | tail -n 1 | awk '{print $3 }' | sed 's/refs\/tags\/blueocean-parent-//')


# Check if the image already exists
if docker pull jenkinsci/blueocean:$BLUEOCEAN_VERSION; then
  echo "Image jenkinsci/blueocean:$BLUEOCEAN_VERSION already exists in Docker Hub"
  exit 0
fi

# ensure that we have the latest Jenkins LTS image available
docker pull jenkins:latest

# Fetch BlueOcean plugins using Maven
#
# Note: we don't use "install-plugins.sh" script from the official Jenkins Docker image because we want to use Maven to resolve
# plugin dependency. For BO we will all blueocean-* plugins to be the same version. The aforementioned script downloads latest version
# of dependent plugins, so we would need to list all BO plugins and do it in the right order (if there's any)
docker run -i --rm -v "$PWD":/usr/src/boplugins -w /usr/src/boplugins maven:3.3.9 -u "$(id -u)" mvn "-Dblueocean.version=$BLUEOCEAN_VERSION" package

# Build the image
docker build --no-cache --pull \
             --tag "jenkinsci/blueocean:$BLUEOCEAN_VERSION" .

# Consider this build is the latest
docker tag -f "jenkinsci/blueocean:$BLUEOCEAN_VERSION" jenkinsci/blueocean:latest

docker push "jenkinsci/blueocean:$BLUEOCEAN_VERSION"
docker push jenkinsci/blueocean:latest
