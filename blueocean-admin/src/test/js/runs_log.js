export const log = `Started by an SCM change
Connecting to GitHub using scherler/******
Cloning the remote Git repository
Cloning repository https://github.com/cloudbees/blueocean-pr-testing.git
 > git init /home/thorsten/src/cloudbees/blueocean/blueocean-plugin/work/jobs/blueocean-pr-testing/branches/develop/workspace@script # timeout=10
Fetching upstream changes from https://github.com/cloudbees/blueocean-pr-testing.git
 > git --version # timeout=10
using .gitcredentials to set credentials
 > git config --local credential.username scherler # timeout=10
 > git config --local credential.helper store --file=/tmp/git7153858628186904051.credentials # timeout=10
 > git -c core.askpass=true fetch --tags --progress https://github.com/cloudbees/blueocean-pr-testing.git +refs/heads/*:refs/remotes/origin/*
 > git config --local --remove-section credential # timeout=10
 > git config remote.origin.url https://github.com/cloudbees/blueocean-pr-testing.git # timeout=10
 > git config --add remote.origin.fetch +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git config remote.origin.url https://github.com/cloudbees/blueocean-pr-testing.git # timeout=10
Fetching upstream changes from https://github.com/cloudbees/blueocean-pr-testing.git
using .gitcredentials to set credentials
 > git config --local credential.username scherler # timeout=10
 > git config --local credential.helper store --file=/tmp/git1466455671723723118.credentials # timeout=10
 > git -c core.askpass=true fetch --tags --progress https://github.com/cloudbees/blueocean-pr-testing.git +refs/heads/*:refs/remotes/origin/*
 > git config --local --remove-section credential # timeout=10
 > git config remote.origin1.url https://github.com/cloudbees/blueocean-pr-testing.git # timeout=10
Fetching upstream changes from https://github.com/cloudbees/blueocean-pr-testing.git
using .gitcredentials to set credentials
 > git config --local credential.username scherler # timeout=10
 > git config --local credential.helper store --file=/tmp/git2350169379680144937.credentials # timeout=10
 > git -c core.askpass=true fetch --tags --progress https://github.com/cloudbees/blueocean-pr-testing.git +refs/pull/*/merge:refs/remotes/origin/pr/*
 > git config --local --remove-section credential # timeout=10
Checking out Revision 59dc3c32ea8807c5b5f35d4ae34bc2968fc22d5c (develop)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f 59dc3c32ea8807c5b5f35d4ae34bc2968fc22d5c
First time build. Skipping changelog.
[Pipeline] Allocate node : Start
Running on master in /home/thorsten/src/cloudbees/blueocean/blueocean-plugin/work/jobs/blueocean-pr-testing/branches/develop/workspace
[Pipeline] node {
[Pipeline] stage (commit - Checkout)
Entering stage commit - Checkout
Proceeding
[Pipeline] checkout
Cloning the remote Git repository
Cloning repository https://github.com/cloudbees/blueocean-pr-testing.git
 > git init /home/thorsten/src/cloudbees/blueocean/blueocean-plugin/work/jobs/blueocean-pr-testing/branches/develop/workspace # timeout=10
Fetching upstream changes from https://github.com/cloudbees/blueocean-pr-testing.git
 > git --version # timeout=10
using .gitcredentials to set credentials
 > git config --local credential.username scherler # timeout=10
 > git config --local credential.helper store --file=/tmp/git576123153900929893.credentials # timeout=10
 > git -c core.askpass=true fetch --tags --progress https://github.com/cloudbees/blueocean-pr-testing.git +refs/heads/*:refs/remotes/origin/*
 > git config --local --remove-section credential # timeout=10
 > git config remote.origin.url https://github.com/cloudbees/blueocean-pr-testing.git # timeout=10
 > git config --add remote.origin.fetch +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git config remote.origin.url https://github.com/cloudbees/blueocean-pr-testing.git # timeout=10
Fetching upstream changes from https://github.com/cloudbees/blueocean-pr-testing.git
using .gitcredentials to set credentials
 > git config --local credential.username scherler # timeout=10
 > git config --local credential.helper store --file=/tmp/git5751187739111346064.credentials # timeout=10
 > git -c core.askpass=true fetch --tags --progress https://github.com/cloudbees/blueocean-pr-testing.git +refs/heads/*:refs/remotes/origin/*
 > git config --local --remove-section credential # timeout=10
 > git config remote.origin1.url https://github.com/cloudbees/blueocean-pr-testing.git # timeout=10
Fetching upstream changes from https://github.com/cloudbees/blueocean-pr-testing.git
using .gitcredentials to set credentials
 > git config --local credential.username scherler # timeout=10
 > git config --local credential.helper store --file=/tmp/git1891955869428389270.credentials # timeout=10
 > git -c core.askpass=true fetch --tags --progress https://github.com/cloudbees/blueocean-pr-testing.git +refs/pull/*/merge:refs/remotes/origin/pr/*
 > git config --local --remove-section credential # timeout=10
Checking out Revision 59dc3c32ea8807c5b5f35d4ae34bc2968fc22d5c (develop)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f 59dc3c32ea8807c5b5f35d4ae34bc2968fc22d5c
First time build. Skipping changelog.
[Pipeline] echo
Test whether you can start it
[Pipeline] } //node
[Pipeline] Allocate node : End
[Pipeline] End of Pipeline

GitHub has been notified of this commitâs build result

Finished: SUCCESS
`
