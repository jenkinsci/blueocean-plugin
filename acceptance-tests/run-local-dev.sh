#!/bin/bash

# ------------------------------------------------------------------------------------------------------------------
# Just a shortcut to run the local version in dev mode
# ------------------------------------------------------------------------------------------------------------------

# Lookup full path of ../blueocean - via perl, because MacOS.

BO_DIR=`perl -MCwd -e 'print Cwd::abs_path shift' ../blueocean`

if [[ ! -d $BO_DIR ]] ; then
    echo "Could not find plugin dir $BO_DIR, aborting."
    exit 1
fi

# Hand off to regular run.sh
./run.sh -a=$BO_DIR --dev "$@"
