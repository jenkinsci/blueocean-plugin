#!/bin/bash

# cd into it
pushd docs
# create a fresh git rep
git init
# add ourself as webpages remote reference
git remote add webpages git@github.com:jenkinsci/blueocean-acceptance-test.git
# get the current online docu
git fetch --depth=1 webpages gh-pages
# add all changes
git add --all
#commit
git commit -m "webpages"
# try to merge
git merge --no-edit -s ours remotes/webpages/gh-pages --allow-unrelated-histories
# push now
git push webpages master:gh-pages
# remove the git dir again
rm -rf .git

popd
