#!/bin/bash

. ./runner/scripts/args.sh
. ./runner/scripts/download.sh
. ./runner/scripts/maven.sh

# ------------------------------------------------------------------------------------------------------------------
# Run acceptance tests.
#
# args:
#
#   -a  Blue Ocean aggregator plugin path
#       e.g. ./run.sh -a=/Users/tfennelly/projects/blueocean/blueocean-plugin
#   -v  Jenkins version
#       e.g. ./run.sh -v=2.5
#   -s  Install local SNAPSHOTS. See https://github.com/jenkinsci/acceptance-test-harness/blob/master/docs/SUT-VERSIONS.md#install-plugins-from-local-maven-repository
#       e.g.  ./run.sh -s
#   -d  Run a clean dev instance of Jenkins (with blueocean plugins) and keep it running. Allows you to do fast dev of tests in your IDE without having to
#       constanly wait for Jenkins to startup etc. Need to ensure that your test does not use JenkinsAcceptanceTestRule while you are developing
#       (e.g. via AbstractJUnitTest), or you'll be wasting your time.
#       e.g.  ./run.sh -d
#
#   All other args are assumed to be customs plugins ala https://github.com/jenkinsci/acceptance-test-harness/blob/master/docs/SUT-VERSIONS.md#use-custom-plugin-file
#   e.g. ./run.sh blueocean-plugin.jpi=/Users/tfennelly/projects/blueocean/blueocean-plugin/target/blueocean-plugin.hpi
#
# ------------------------------------------------------------------------------------------------------------------
npm install
assemble-plugins $AGGREGATOR_DIR

# Download the jenkins war
download "http://mirrors.jenkins-ci.org/war-stable/${JENKINS_VERSION}/jenkins.war" "bin/jenkins-${JENKINS_VERSION}.war"

if [ "${RUN_SELENIUM}" == "true" ]; then
    ./runner/scripts/start-selenium.sh
fi


EXECUTION="env JENKINS_JAVA_OPTS=\"${JENKINS_JAVA_OPTS}\" ${ATH_SERVER_HOST} ${ATH_SERVER_PORT} BROWSER=phantomjs LOCAL_SNAPSHOTS=${LOCAL_SNAPSHOTS} ${PLUGINS} PLUGINS_DIR=../runtime-plugins/runtime-deps/target/plugins-combined PATH=./node:./node/npm/bin:./node_modules/.bin:${PATH} JENKINS_WAR=../bin/jenkins-${JENKINS_VERSION}.war mvn -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -B -Dmaven.test.failure.ignore ${MAVEN_SETTINGS} test ${PROFILES} ${TEST_TO_RUN}"

echo ""
echo "> ${EXECUTION}"
echo ""

echo "------------------------------------------------"

# Run the tests
pushd runner/runtime
EXIT_CODE=0
eval "${EXECUTION}"
if [ $? != 0 ];then
    EXIT_CODE=1
fi
popd


if [ "${RUN_SELENIUM}" == "true" ]; then
    ./runner/scripts/stop-selenium.sh
fi

exit $EXIT_CODE
