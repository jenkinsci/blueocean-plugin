Acceptance tests for Blue Ocean

# Prepare your system

## Install xmllint/libssl-dev

You need to install xmllint and libssl-dev prior of running the server!

In debian:

```
apt-get install libxml2-utils libssl-dev 
```

# Building

To build:

```sh
mvn clean install -DskipTests
```

For now, you __must__ build with the `-DskipTests` switch. We'll be able to change this once the Blue Ocean
plugins are in the Update Center.


# Running
Builds directly on the main Jenkins [acceptance-test-harness](https://github.com/jenkinsci/acceptance-test-harness),
so running it works as per the instructions on the README.md there.

> __NOTE:__ If you are not running dev mode, we assume a running selenium instance! See above how to start selenium manually.

We also added a shortcut script for easy running:

```sh
./run.sh -a=<aggregator-plugin-path>
```

The `-a` (or `--aggregator`) switch is the path to the Blue Ocean aggregator plugin
(e.g. `/Users/tfennelly/projects/blueocean/blueocean-plugin/blueocean-plugin`). This switch is needed until such time as the
Blue Ocean plugins are in the Update Center.

> __NOTE:__ You __must__ build the [blueocean-plugin repo](https://github.com/jenkinsci/blueocean-plugin) code before running.

That script will download a recent version of Jenkins from the download mirror (see script) and will run the acceptance
tests. Alternatively, you can specify the the version of Jenkins that you want to test against e.g. to run the tests
against Jenkins version `2.4` (again, it will download that version of Jenkins if it doesn't already have it in the
`wars` directory):
 
```sh
./run.sh --version=2.4 -a=<aggregator-plugin-path>
```

# Running on Windows

Sorry, this is not supported at the moment. We'd need to create equivalents of the shell scripts we already
have in this project for running on Linux/MacOS. Feel like you'd be interested in doing that?

# Troubleshooting

> (might move this section to an FAQ)

## Tests/Browser hangs when running on my local development machine

You need to determine if this is an issue with a change to the test harness itself, or as a result of a change to the
local environment e.g. a browser version update (most common cause).

Things to check:

1. Are the acceptance tests passing on the CI server (https://ci.blueocean.io/). If they're failing there, then it might not be a local env issue.
1. Did the browser auto-update to a version that has Selenium compatibility issues. We have seen issues with Firefox versions (e.g. Firefox 47.x and Selenium 2.53). As well as upgrading/downgrading the browser version to something that's compatible with the Selenium version, it might be a good idea to turn off browser auto-updates, otherwise there's a good chance of hitting issues again.
1. If you have local changes, stash/revert them and try a version that was known to work previously.

# Running in dev mode

When running in normal mode, tests are run via JUnit and `NightwatchTest`
(which uses the `JenkinsAcceptanceTestRule` from the main acceptance test harness). This is good
when running all of the tests (e.g. CI builds) because it launches a clean Jenkins (clean `JENKINS_HOME` etc) for every
run/suite. The downside to this however is that it's not such an easy model to use when developing tests
because of the overhead of starting a new Jenkins every time (30+ seconds every time).

For this we have a "dev" mode option which allows you to run a Jenkins instance in the background, keeping it
running and then, in another terminal, to run [nightwatch] commands (a Selenium JS framework) to
run tests quickly as you are writing them.

To run in dev mode, simply add the `--dev` (or just `-d`) switch e.g.
 
```sh
./run.sh -a=<aggregator-plugin-path> --dev
```

An example of one of the [nightwatch] test scripts is [smoke.js](src/test/js/smoke.js). It is hooked into
the main (maven) build via [SmokeTest.java](src/test/java/io/jenkins/blueocean/SmokeTest.java),
but to run [smoke.js](src/test/js/smoke.js) while running in dev mode, simply run:

```sh
nightwatch src/test/js/smoke.js
```

Of course this assumes you have the [nightwatch] package globally installed (`npm install -g nightwatch`).
Alternatively, you can just run `npm test` to run all [nightwatch] tests.

[![Video](http://img.youtube.com/vi/o8r4ztgpm8E/maxresdefault.jpg)](https://youtu.be/o8r4ztgpm8E)

# Client code log output

When running in `--dev` mode, it can be useful to turn on client code log output. To do this, simply set
the `LOG_CONFIG` env variable e.g. to turn on SSE logging:

```
$ export LOG_CONFIG=sse
$ nightwatch ./src/test/js/karaoke-*.js
```

[nightwatch]: http://nightwatchjs.org/
