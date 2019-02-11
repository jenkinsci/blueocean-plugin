#!/bin/bash
set -e

echo ""
echo " Stopping old Bitbucket Docker container, if any. This may take a few seconds..."
docker stop blueo-bitbucket > /dev/null || true
docker rm blueo-bitbucket > /dev/null || true
echo "      ... stopped"
echo ""
