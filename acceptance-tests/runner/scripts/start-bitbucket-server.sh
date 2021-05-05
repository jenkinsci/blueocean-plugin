#!/bin/bash
set -e

SCRIPT_DIR=$(dirname $0)
$SCRIPT_DIR/stop-bitbucket-server.sh

# local cli
if [ "${LOCAL_DEV}" == "true" ]; then
  echo ""
  echo " Starting Bitbucket Docker container local net..."
  echo ""
  docker run --name blueo-bitbucket -d -t -p 7990:7990 hadashi/bitbucket-server
else
  echo ""
  echo " Starting Bitbucket Docker container..."
  echo ""
  docker run --net=host -e BITBUCKET_HOME=/var/atlassian --name="blueo-bitbucket" -d hadashi/bitbucket-server
fi

#docker run --name blueo-bitbucket -d -t -p 7990:7990 hadashi/blueocean-bitbucket-server  atlas-run-standalone  -u 6.3.0 --product bitbucket --version 5.2.0 --data-version 5.2.0 --jvmargs -Xmx4096M -DskipAllPrompts
#SELENIUM_IP=`docker inspect -f '{{ .NetworkSettings.IPAddress }}' blueo-selenium`
#mkdir -p ./target
#echo $SELENIUM_IP > ./target/.selenium_ip
