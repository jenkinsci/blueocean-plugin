#!/bin/bash

echo ""
echo " Stopping old Bitbucket Docker container, if any. This may take a few seconds..."
docker stop blueo-bitbucket > /dev/null
docker rm blueo-bitbucket > /dev/null
echo "      ... stopped"
echo ""
