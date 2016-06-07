#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# todo - check for mvn being installed

# todo - check for version of maven installed

# todo - check for version of java installed

mvn clean install "$@"
