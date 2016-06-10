#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# todo - check if maven clean install has been run prior

cd ${DIR}/blueocean-plugin \
&& mvn hpi:run
