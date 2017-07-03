JENKINS_VERSION=2.7.4
SELENIUM_VERSION=2.53

MAVEN_SETTINGS=""
LOCAL_SNAPSHOTS=false
RUN_SELENIUM=true
ATH_SERVER_HOST=""
ATH_SERVER_PORT=""
PLUGINS=""
AGGREGATOR_DIR=""
DEV_JENKINS=false
PROFILES="-P runTests"
JENKINS_JAVA_OPTS="-Djava.util.logging.config.file=./logging.properties"
TEST_TO_RUN=""

for i in "$@"
do
case $i in
    -a=*|--aggregator=*)
    AGGREGATOR_DIR="${i#*=}"
    ;;
    -v=*|--version=*)
    JENKINS_VERSION="${i#*=}"
    ;;
    -s|--snaps|--snapshots)
    LOCAL_SNAPSHOTS=true
    ;;
    --no-selenium)
    RUN_SELENIUM=false
    ;;
    --settings=*)
    MAVEN_SETTINGS="${i#*=}"
    ;;
    -h=*|--host=*)
    ATH_SERVER_HOST="${i#*=}"
    ;;
    -p=*|--port=*)
    ATH_SERVER_PORT="${i#*=}"
    ;;
    -d|--dev)
    DEV_JENKINS=true
    ;;
    --default)
    ;;
    *)
    PLUGINS="${PLUGINS} $i" && LOCAL_SNAPSHOTS=true
    ;;
esac
done

if [ "${DEV_JENKINS}" == "true" ]; then
    echo ""
    echo "*****"
    echo "***** Starting a test dev instance of Jenkins with the blueocean plugins."
    echo "***** Watch console output for the URL to use while developing your test."
    echo "***** You can debug the Jenkins instance on port 15000."
    echo "*****"
    echo "***** Be sure your test doesn't use JenkinsAcceptanceTestRule in any way"
    echo "***** or you'll be defeating the purpose (e.g. via AbstractJUnitTest)."
    echo "*****"
    echo ""

    JENKINS_JAVA_OPTS="${JENKINS_JAVA_OPTS} -Xrunjdwp:transport=dt_socket,server=y,address=15000,suspend=n"

    # Exclude the actual tests from this run and just run the skeleton test in
    # ExcludedRunnerTest. It will just run with the plugins and stay running
    # allowing you to iterate on your acceptance test dode without constantly
    # having to restart Jenkins.
    PROFILES="-P runDevRunner"
    TEST_TO_RUN=""
fi


# For now, the location of the aggregator plugin must be defined until we have
# blueocean plugins in the Update Center.
if [ "${AGGREGATOR_DIR}" == "" ]; then
    AGGREGATOR_DIR="../blueocean/"
fi
if [ ! -d "${AGGREGATOR_DIR}" ]; then
    echo ""
    echo " *********************************************************************"
    echo "    The Blue Ocean aggregator plugin location is not a"
    echo "    valid directory."
    echo " *********************************************************************"
    echo ""
    exit 1
fi

if [ ! -f "${AGGREGATOR_DIR}/.pre-assembly" ]; then
    pushd "${AGGREGATOR_DIR}"
    AGGREGATOR_GROUP_ID=`echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:parent/x:groupId/text()' | xmllint --shell pom.xml | grep -v /`
    AGGREGATOR_ARTIFACT_ID=`echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:artifactId/text()' | xmllint --shell pom.xml | grep -v /`
    popd
    if [ "${AGGREGATOR_GROUP_ID}" != "io.jenkins.blueocean" ] || [ "${AGGREGATOR_ARTIFACT_ID}" != "blueocean" ]; then
        echo ""
        echo " *********************************************************************"
        echo "    The location specified for the aggregator plugin does not appear"
        echo "    to be correct. Check the supplied parameter and make sure it"
        echo "    points to the aggregator plugin."
        echo "    > groupId:    ${AGGREGATOR_GROUP_ID}"
        echo "    > artifactId: ${AGGREGATOR_ARTIFACT_ID}"
        echo " *********************************************************************"
        echo ""
        exit 1
    fi
fi

echo "------------------------------------------------"
echo "Running with switches:"
echo "    --version=${JENKINS_VERSION}"
echo "    --snapshots=${LOCAL_SNAPSHOTS}"
echo "    --aggregator=${AGGREGATOR_DIR}"
echo "    --dev=${DEV_JENKINS}"

if [ "${PLUGINS}" == "" ]; then
    echo ""
    echo "    No local plugins specified. E.g.:"
    echo "    ./run.sh blueocean-plugin.jpi=../blueocean/blueocean-plugin/target/blueocean-plugin.hpi"
    echo ""
fi

if [ "${ATH_SERVER_HOST}" != "" ]; then
    ATH_SERVER_HOST="blueoceanHost=${ATH_SERVER_HOST}"
fi
if [ "${ATH_SERVER_PORT}" != "" ]; then
    ATH_SERVER_PORT="httpPort=${ATH_SERVER_PORT}"
fi
