#!/bin/bash
set -e
echo "*"
echo "* This will use gulp to watch for changes"
echo "* and run mvn hpi:run on the core module."
echo "*"
echo "* Ensure you have gulp installed: http://gulpjs.com/"

trap ctrl_c INT

function ctrl_c() {
    echo "** CLEANUP: killing gulp rebundle process."
    kill $MVN_PID
    echo "Cleanup finished."
}

cd core

mvn hpi:run &
MVN_PID=$!

echo "** Starting gulp to watch for changes"
gulp rebundle
