#!/bin/bash

download() {
    pushd runner
    if [ ! -d bin ]; then
        mkdir bin
    fi
    URL=$1
    DOWNTO=$2
    if [ -f "$DOWNTO" ]; then
        echo ""
        echo "***** ${DOWNTO} already downloaded. Skipping download."
        echo ""
        popd
        return
    fi

    echo ""
    echo "Downloading ${URL} to ${DOWNTO} ..."
    echo ""

    # Try for curl, then wget, or fail
    if hash curl 2>/dev/null;
    then
        curl -o $DOWNTO -L $URL
    elif hash wget 2>/dev/null;
    then
        wget -O $DOWNTO $URL
    else
        popd
        echo "curl or wget must be installed."
        exit 1
    fi

    if [ $? != 0 ]
      then
        popd
        echo " ************************************"
        echo " **** ${DOWNTO} download failed."
        echo " ************************************"
        exit 1
    fi

     popd
}

download-jenkins() {
    pushd runner
    if [ ! -d bin ]; then
        mkdir bin
    fi
    JENKINS_VERSION=$1
    DOWNTO=$2
    if [ -f "$DOWNTO" ]; then
        echo ""
        echo "***** ${DOWNTO} already downloaded. Skipping download."
        echo ""
        popd
        return
    fi

    echo ""
    echo "Downloading Jenkins ${JENKINS_VERSION} WAR to ${DOWNTO} ..."
    echo ""

    mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:copy -Dartifact="org.jenkins-ci.main:jenkins-war:${JENKINS_VERSION}:war" -DoutputDirectory=target/dependency ${MAVEN_SETTINGS} && cp "target/dependency/jenkins-war-${JENKINS_VERSION}.war" "${DOWNTO}"

    if [ $? != 0 ]
      then
        echo " ************************************"
        echo " **** ${DOWNTO} download failed."
        echo " ************************************"
        popd
        exit 1
    fi
    
    popd
}

