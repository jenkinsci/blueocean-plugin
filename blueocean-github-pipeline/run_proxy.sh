#!/bin/sh
# Note: run me from same directory
echo $SCRIPT
nginx -c $PWD/nginx.conf
