#!/bin/bash

SCRIPT_DIR=$(dirname $0)
$SCRIPT_DIR/stop-bitbucket-server.sh

echo ""
echo " Starting Bitbucket Docker container..."
echo ""
docker run --name blueo-bitbucket -d -t -p 7990:7990 hadashi/blueocean-bitbucket-server  atlas-run-standalone  -u 6.3.0 --product bitbucket --version 5.2.0 --data-version 5.2.0 --jvmargs -Xmx4096M -DskipAllPrompts
#SELENIUM_IP=`docker inspect -f '{{ .NetworkSettings.IPAddress }}' blueo-selenium`
#mkdir -p ./target
#echo $SELENIUM_IP > ./target/.selenium_ip
