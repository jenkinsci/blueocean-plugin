#!/bin/sh

if [[ "$@" == "--debug" ]]
then
  cmd=mvnDebug
else
  cmd=mvn
fi

export MAVEN_OPTS="-Dblueocean.config.file=../app.properties"
cd all
$cmd hpi:run
