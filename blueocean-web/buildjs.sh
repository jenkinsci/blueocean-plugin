#!/bin/bash

pushd ..
npx lerna clean -y
npx lerna bootstrap
npx lerna link --force-local
popd
npx webpack --mode production