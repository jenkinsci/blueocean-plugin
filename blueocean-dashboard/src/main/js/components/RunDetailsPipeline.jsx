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
            this.props.fetchNodes(this.generateConfig());
        }
    }

    generateConfig() {
        const {
              params: { pipeline: name, branch, runId, node: nodeParam },
              config = {},
        } = this.context;
        const { isMultiBranch, nodeId } = this.props;
        // if we have a node param we do not want the calculation of the focused node
        const node = nodeParam || nodeId;
        const mergedConfig = { ...config, name, branch, runId, isMultiBranch, node };
        return mergedConfig;
    }

    render() {
        const { pipeline: name, branch, runId } = this.context.params;
        const { isMultiBranch, steps, nodes } = this.props;

        if (!steps) {
            return null;
        }
        const mergedConfig = this.generateConfig();
        const nodeKey = calculateNodeBaseUrl(mergedConfig);
        const key = calculateStepsBaseUrl(mergedConfig);
        const logGeneral = calculateRunLogURLObject(mergedConfig);

        return (
          <div>
            { nodes && nodes[nodeKey] && <ExtensionPoint
              name="jenkins.pipeline.run.result"
              nodes={nodes[nodeKey].model}
              pipelineName={name}
              branchName={isMultiBranch ? branch : undefined}
              runId={runId}
            />
            }
            <LogToolbar fileName={logGeneral.fileName} url={logGeneral.url} />
            { steps && <Steps
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
    fileName: string,
    url: string,
    fetchNodes: func,
    steps: any,
    nodes: any,
    nodeId: any,
};

RunDetailsPipeline.contextTypes = {
    config: object.isRequired,
    params: object,
    pipeline: object,
};

const selectors = createSelector(
    [stepsSelector, logSelector, nodeSelector, nodesSelector],
    (steps, logs, nodeId, nodes) => ({ steps, logs, nodeId, nodes }));

export default connect(selectors, actions)(RunDetailsPipeline);
