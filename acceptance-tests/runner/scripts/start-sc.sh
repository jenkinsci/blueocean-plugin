#!/bin/bash

SCRIPT_DIR=$(dirname $0)
$SCRIPT_DIR/stop-sc.sh

echo ""
echo " Starting Sauce Connect container..."
echo ""
docker run \
    -d \
    --name blueo-selenium \
    --net host \
    -e SAUCE_ACCESS_KEY \
    -e SAUCE_USERNAME \
    -e BUILD_TAG \
    --rm \
    blueocean/sauceconnect:4.5.3
