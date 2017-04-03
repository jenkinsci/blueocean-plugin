# In the root directory
# ln -s `pwd`/bin/pre-push.sh .git/hooks/pre-push
node checkdeps.js
RESULT=$?
[ $RESULT -ne 0 ] && exit 1
exit 0
