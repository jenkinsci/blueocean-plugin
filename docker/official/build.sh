#!/bin/bash -xe

HERE=$(dirname $0)
cd $HERE

BLUEOCEAN_VERSION=$(git for-each-ref --sort=-taggerdate refs/tags/blueocean-parent-\*  | grep -v 'beta' | head -1 |awk '{print $3 }' | sed 's/refs\/tags\/blueocean-parent-//')

# Check if the image already exists
if docker pull jenkinsci/blueocean:$BLUEOCEAN_VERSION; then
  echo "Image jenkinsci/blueocean:$BLUEOCEAN_VERSION already exists in Docker Hub"
  exit 0
fi

# Build the image
docker build --rm --no-cache --pull \
             --tag "jenkinsci/blueocean:$BLUEOCEAN_VERSION" .

# Consider this build is the latest
docker tag "jenkinsci/blueocean:$BLUEOCEAN_VERSION" jenkinsci/blueocean:latest

docker push "jenkinsci/blueocean:$BLUEOCEAN_VERSION"
docker push jenkinsci/blueocean:latest
