import React, { Component, PropTypes } from 'react';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';
import LogConsole from './LogConsole';

import Steps from './Steps';
import {
    steps as stepsSelector,
    logs as logSelector,
    node as nodeSelector,
    nodes as nodesSelector,
    actions,
    calculateRunLogURLObject,
    calculateNodeBaseUrl,
    calculateStepsBaseUrl,
    connect,
    createSelector,
} from '../redux';

import LogToolbar from './LogToolbar';

const { string, object, any, func } = PropTypes;

export class RunDetailsPipeline extends Component {
    componentWillMount() {
        const { fetchNodes, fetchLog, result } = this.props;
        const mergedConfig = this.generateConfig(this.props);

        if (result && result._class === 'io.jenkins.blueocean.service.embedded.rest.PipelineRunImpl') {
            fetchNodes(mergedConfig);
        } else {
            // console.log('fetch the log directly')
            const logGeneral = calculateRunLogURLObject(mergedConfig);
            fetchLog({ ...logGeneral });
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.params.node !== this.props.params.node) {
            const config = this.generateConfig(nextProps);
            this.props.setNode(config);
            this.props.fetchSteps(config);
        }

        const { logs, fetchLog } = nextProps;
        if (logs !== this.props.logs) {
            const mergedConfig = this.generateConfig(nextProps);
            const logGeneral = calculateRunLogURLObject(mergedConfig);
            const log = logs ? logs[logGeneral.url] : null;
            if (log && log !== null) {
                const newStart = log.newStart;
                if (Number(newStart) > 0) {
                    // kill current  timeout if any
                    clearTimeout(this.timeout);
                    this.timeout = setTimeout(() => fetchLog({ ...logGeneral, newStart }), 1000);
                }
            }
        }
    }

    componentWillUnmount() {
        this.props.cleanNodePointer();
        clearTimeout(this.timeout);
    }

    generateConfig(props) {
        const {
            config = {},
        } = this.context;
        const {
            isMultiBranch,
            params: { pipeline: name, branch, runId, node: nodeParam },
        } = props;
        // we would use default properties however the node can be null so no default properties will be triggered
        let { nodeReducer } = props;
        if (!nodeReducer) {
            nodeReducer = { id: null, displayName: 'Steps' };
        }
        // if we have a node param we do not want the calculation of the focused node
        const node = nodeParam || nodeReducer.id;
        // we turn on refetch so we always fetch a new Node result, will be changed to props
        const refetch = true;
        const mergedConfig = { ...config, name, branch, runId, isMultiBranch, node, nodeReducer, refetch };
        return mergedConfig;
    }

    render() {
        const {
            location,
            router,
        } = this.context;

        const {
            params: {
                pipeline: name, branch, runId,
            },
            isMultiBranch, steps, nodes, logs, result: resultMeta,
        } = this.props;

        const {
          result,
          state,
        } = resultMeta;
        const resultRun = result === 'UNKNOWN' || !result ? state : result;
        const scrollToBottom = resultRun.toLowerCase() === 'failure' || resultRun.toLowerCase() === 'running';

        const mergedConfig = this.generateConfig(this.props);

        const nodeKey = calculateNodeBaseUrl(mergedConfig);
        const key = calculateStepsBaseUrl(mergedConfig);
        const logGeneral = calculateRunLogURLObject(mergedConfig);
        const log = logs ? logs[logGeneral.url] : null;
        let title = mergedConfig.nodeReducer.displayName;
        if (log) {
            title = 'Logs';
        } else if (mergedConfig.nodeReducer.id !== null) {
            title = `Steps - ${title}`;
        }
        return (
            <div>
                { nodes && nodes[nodeKey] && <ExtensionPoint
                  router={router}
                  location={location}
                  name="jenkins.pipeline.run.result"
                  nodes={nodes[nodeKey].model}
                  pipelineName={name}
                  branchName={isMultiBranch ? branch : undefined}
                  runId={runId}
                />
                }
                <LogToolbar
                  fileName={logGeneral.fileName}
                  url={logGeneral.url}
                  title={title}
                />
                { steps && steps[key] && <Steps
                  nodeInformation={steps[key]}
                  {...this.props}
                />
                }

                { log && <LogConsole key={logGeneral.url} logArray={log.logArray} scrollToBottom={scrollToBottom} /> }
            </div>
        );
    }
}

RunDetailsPipeline.propTypes = {
    pipeline: object,
    isMultiBranch: any,
    params: object,
    result: object,
    fileName: string,
    url: string,
    fetchLog: func,
    fetchNodes: func,
    setNode: func,
    fetchSteps: func,
    cleanNodePointer: func,
    logs: object,
    steps: object,
    nodes: object,
    nodeReducer: object,
};

RunDetailsPipeline.contextTypes = {
    config: object.isRequired,
    params: object,
    pipeline: object,
    router: object.isRequired, // From react-router
    location: object.isRequired, // From react-router
};

const selectors = createSelector(
    [stepsSelector, logSelector, nodeSelector, nodesSelector],
    (steps, logs, nodeReducer, nodes) => ({ steps, logs, nodeReducer, nodes }));

export default connect(selectors, actions)(RunDetailsPipeline);
