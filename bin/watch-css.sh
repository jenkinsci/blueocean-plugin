#!/bin/bash -xe

DIR="$( cd "$( dirname "$0" )" && pwd )"

cd ${DIR}/../blueocean-web
npm install ../blueocean-core-js
npm install -g slink

printf "running gulp watch\n"
cd ${DIR}/../blueocean-core-js && nohup gulp watch &

printf "running slink\n"
cd ${DIR}/../blueocean-web
slink ../blueocean-core-js
