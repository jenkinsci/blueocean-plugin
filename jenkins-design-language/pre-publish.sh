#!/bin/bash
set -e

# This fix makes baby Jesus cry: We need to test for "node_modules" because npm will do
# a "prepublish" when pulling in a local dep, but doesn't run "install" first... 
# It *also* liket to run "prepublish" after "install" which gives us a loop. 
test -d "node_modules" || npm install 
npm run gulp
