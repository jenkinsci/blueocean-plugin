export const log = `<!-- saved from url=(0120)http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/blueocean-pr-testing/branches/master/runs/8/log/ -->
Started by an SCM change
Connecting to GitHub using scherler/******
 &gt; git rev-parse --is-inside-work-tree # timeout=10
Fetching changes from 2 remote Git repositories
 &gt; git config remote.origin.url <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> # timeout=10
Fetching upstream changes from <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a>
 &gt; git --version # timeout=10
using .gitcredentials to set credentials
 &gt; git config --local credential.username scherler # timeout=10
 &gt; git config --local credential.helper store --file=/tmp/git3157926345903194824.credentials # timeout=10
 &gt; git -c core.askpass=true fetch --tags --progress <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> +refs/heads/*:refs/remotes/origin/*
 &gt; git config --local --remove-section credential # timeout=10
 &gt; git config remote.origin1.url <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> # timeout=10
Fetching upstream changes from <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a>
using .gitcredentials to set credentials
 &gt; git config --local credential.username scherler # timeout=10
 &gt; git config --local credential.helper store --file=/tmp/git4301199164301375710.credentials # timeout=10
 &gt; git -c core.askpass=true fetch --tags --progress <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> +refs/pull/*/merge:refs/remotes/origin/pr/*
 &gt; git config --local --remove-section credential # timeout=10
Checking out Revision 26db198b016b3bcc4425792b11d5da041f24fb8d (master)
 &gt; git config core.sparsecheckout # timeout=10
 &gt; git checkout -f 26db198b016b3bcc4425792b11d5da041f24fb8d
 &gt; git rev-list f82c2268f6adaa9b9498730aed0aa007c37f1212 # timeout=10
<span style="color:#9A9999">[Pipeline] Allocate node : Start
</span>Still waiting to schedule task
Waiting for next available executor
Running on master in /home/thorsten/src/cloudbees/blueocean/blueocean-plugin/work/jobs/blueocean-pr-testing/branches/master/workspace
<span style="color:#9A9999">[Pipeline] node {
</span><span style="color:#9A9999">[Pipeline] stage (commit - Checkout)
</span>Entering stage commit - Checkout
Proceeding
<span style="color:#9A9999">[Pipeline] checkout
</span> &gt; git rev-parse --is-inside-work-tree # timeout=10
Fetching changes from 2 remote Git repositories
 &gt; git config remote.origin.url <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> # timeout=10
Fetching upstream changes from <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a>
 &gt; git --version # timeout=10
using .gitcredentials to set credentials
 &gt; git config --local credential.username scherler # timeout=10
 &gt; git config --local credential.helper store --file=/tmp/git2509577548057893295.credentials # timeout=10
 &gt; git -c core.askpass=true fetch --tags --progress <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> +refs/heads/*:refs/remotes/origin/*
 &gt; git config --local --remove-section credential # timeout=10
 &gt; git config remote.origin1.url <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> # timeout=10
Fetching upstream changes from <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a>
using .gitcredentials to set credentials
 &gt; git config --local credential.username scherler # timeout=10
 &gt; git config --local credential.helper store --file=/tmp/git3317547532899248622.credentials # timeout=10
 &gt; git -c core.askpass=true fetch --tags --progress <a href="https://github.com/cloudbees/blueocean-pr-testing.git">https://github.com/cloudbees/blueocean-pr-testing.git</a> +refs/pull/*/merge:refs/remotes/origin/pr/*
 &gt; git config --local --remove-section credential # timeout=10
Checking out Revision 26db198b016b3bcc4425792b11d5da041f24fb8d (master)
 &gt; git config core.sparsecheckout # timeout=10
 &gt; git checkout -f 26db198b016b3bcc4425792b11d5da041f24fb8d
<span style="color:#9A9999">[Pipeline] echo
</span>Test whether you can start it
<span style="color:#9A9999">[Pipeline] sh
</span>[workspace] Running shell script
+ date
Thu Apr  7 02:41:31 CEST 2016
+ sleep 310
+ date
Thu Apr  7 02:46:41 CEST 2016
<span style="color:#9A9999">[Pipeline] } //node
</span><span style="color:#9A9999">[Pipeline] Allocate node : End
</span><span style="color:#9A9999">[Pipeline] End of Pipeline
</span>
GitHub has been notified of this commitâs build result

Finished: SUCCESS`
