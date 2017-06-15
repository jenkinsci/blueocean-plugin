#!/bin/bash -xe

HERE=$(dirname $0)
cd $HERE

# Useful to rebuild an already existing image, in case it's broken
FORCE_REBUILD=${FORCE_REBUILD:-$false}
BLUEOCEAN_VERSION=$(git for-each-ref --sort=-taggerdate refs/tags/blueocean-parent-\*  | grep -v 'beta' | head -1 |awk '{print $3 }' | sed 's/refs\/tags\/blueocean-parent-//')

# Check we have the latest LTS for blue ocean to build on and get its image ID
docker pull jenkinsci/jenkins:lts-alpine
LTS_IMAGE_ID=$(docker images jenkinsci/jenkins:lts-alpine | sed -n 2p | awk '{ print $3 }')

# Make an explicit version tag for both blue and LTS, like 1.0.1-3321321
# We need this so if the LTS image changes OR blue ocean, we rebuild and published
# A new LTS means there may be security fixes
FULL_VERSION="$BLUEOCEAN_VERSION-$LTS_IMAGE_ID"

if [ ! $FORCE_REBUILD ]; then
  # Check if the image already exists
  if docker pull jenkinsci/blueocean:$FULL_VERSION; then
    echo "Image jenkinsci/blueocean:$FULL_VERSION already exists in Docker Hub"
    exit 0
  fi
fi

# Build the image
docker build --rm --no-cache --pull \
             --tag "jenkinsci/blueocean:$BLUEOCEAN_VERSION" .

# Consider this build is the latest
docker tag "jenkinsci/blueocean:$BLUEOCEAN_VERSION" jenkinsci/blueocean:latest

# Tag this build with the full version tag
docker tag "jenkinsci/blueocean:$BLUEOCEAN_VERSION" "jenkinsci/blueocean:$FULL_VERSION"

# push it real good
docker push "jenkinsci/blueocean:$BLUEOCEAN_VERSION"
docker push "jenkinsci/blueocean:$FULL_VERSION"
docker push jenkinsci/blueocean:latest
