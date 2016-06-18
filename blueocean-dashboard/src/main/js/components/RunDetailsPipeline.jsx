import React, { Component, PropTypes } from 'react';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';
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
        if (this.props.fetchNodes) {
            this.props.fetchNodes(this.generateConfig(this.props));
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.params.node !== this.props.params.node) {
            const config = this.generateConfig(nextProps);
            this.props.setNode(config);
            this.props.fetchSteps(config);
        }
    }

    componentWillUnmount() {
        this.props.cleanNodePointer();
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
            nodeReducer = {id: null, displayName: 'default title'};
        }
        // if we have a node param we do not want the calculation of the focused node
        const node = nodeParam || nodeReducer.id;
        const mergedConfig = { ...config, name, branch, runId, isMultiBranch, node, nodeReducer };
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
            isMultiBranch, steps, nodes,
        } = this.props;

        if (!steps) {
            return null;
        }
        const mergedConfig = this.generateConfig(this.props);

        const nodeKey = calculateNodeBaseUrl(mergedConfig);
        const key = calculateStepsBaseUrl(mergedConfig);
        const logGeneral = calculateRunLogURLObject(mergedConfig);
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
                    title={mergedConfig.nodeReducer.displayName}
                />
                { steps && steps[key] && <Steps
                  nodeInformation={steps[key]}
                  {...this.props}
                />
                }
            </div>
        );
    }
}

RunDetailsPipeline.propTypes = {
    pipeline: object,
    isMultiBranch: any,
    params: object,
    fileName: string,
    url: string,
    fetchNodes: func,
    setNode: func,
    fetchSteps: func,
    cleanNodePointer: func,
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
