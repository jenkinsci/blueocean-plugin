#!/usr/bin/env bash


assemble-plugins() {
    AGGREGATOR_DIR=$1

    if [ "${AGGREGATOR_DIR}" != "" ]; then
        if [ ! -f "${AGGREGATOR_DIR}/.pre-assembly" ]; then
            echo ""
            echo "Assembling aggregator plugin dependencies..."
            echo ""
            pushd "${AGGREGATOR_DIR}"
            mvn hpi:assemble-dependencies
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
    mvn clean install -DskipTests
    pushd runtime-deps
    mvn hpi:assemble-dependencies
    popd
    popd

    cp -f $AGGREGATOR_DIR/target/plugins/*.hpi ./runner/runtime-plugins/runtime-deps/target/plugins
}