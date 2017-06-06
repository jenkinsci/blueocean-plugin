#!/usr/bin/env bash


assemble-plugins() {
    AGGREGATOR_DIR=$1

    if [ "${AGGREGATOR_DIR}" != "" ]; then
        if [ ! -f "${AGGREGATOR_DIR}/.pre-assembly" ]; then
            echo ""
            echo "Assembling aggregator plugin dependencies..."
            echo ""
            pushd "${AGGREGATOR_DIR}"
            mvn hpi:assemble-dependencies -B -DjenkinsCoreVersionOverride=$JENKINS_VERSION -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
            if [ $? != 0 ];then
                echo "*****"
                echo "***** Error assembling dependencies from aggregator plugin. Maybe you need to rebuild everything."
                echo "*****"
                exit 1
            fi
            popd
        fi
    fi
    echo asdfasdfad $PWD
    echo "Assembling ATH dependency plugins (non Blue Ocean) ..."
    pushd runner/runtime-plugins
    mvn clean install -B -DskipTests -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
    pushd runtime-deps
    mvn hpi:assemble-dependencies -B -DjenkinsCoreVersionOverride=$JENKINS_VERSION -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
    popd
    popd

    mkdir -p ./runner/runtime-plugins/runtime-deps/target/plugins-combined
    cp -f $AGGREGATOR_DIR/target/plugins/* ./runner/runtime-plugins/runtime-deps/target/plugins-combined
    cp -f ./runner/runtime-plugins/runtime-deps/target/plugins/* ./runner/runtime-plugins/runtime-deps/target/plugins-combined
}
