#!/bin/bash

echo ""
echo " Stopping old Sauce Connect container, if any. This may take a few seconds..."
docker stop blueo-selenium > /dev/null
docker rm blueo-selenium > /dev/null
echo "      ... stopped"
echo ""
