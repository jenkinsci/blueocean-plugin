import React, { Component, PropTypes } from 'react';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';
import Steps from './Steps';
import {
    steps as stepsSelector,
    logs as logSelector,
    node as nodeSelector,
    actions,
    calculateRunLogURLObject,
    calculateStepsBaseUrl,
    connect,
    createSelector,
} from '../redux';

import LogToolbar from './LogToolbar';

const { string, object, any, func } = PropTypes;

export class RunDetailsPipeline extends Component {
    componentWillMount() {
        if (this.context.params.node) {
            this.props.fetchSteps(this.generateConfig());
        } else {
            if (this.props.fetchNodes) {
                this.props.fetchNodes(this.generateConfig());
            }
        }
    }

    generateConfig() {
        const {
              params: { pipeline: name, branch, runId, node: nodeParam },
              config = {},
        } = this.context;
        const { isMultiBranch, nodeId } = this.props;
        const node = nodeParam || nodeId;
        const mergedConfig = { ...config, name, branch, runId, isMultiBranch, node };
        return mergedConfig;
    }

    render() {
        const { pipeline: name, branch, runId } = this.context.params;
        const { isMultiBranch, steps } = this.props;

        if (!steps) {
            return null;
        }
        const mergedConfig = this.generateConfig();
        const key = calculateStepsBaseUrl(mergedConfig);
        const logGeneral = calculateRunLogURLObject(mergedConfig);
        return (
          <div>
            <ExtensionPoint name="jenkins.pipeline.run.result"
              pipelineName={name}
              branchName={isMultiBranch ? branch : undefined}
              runId={runId}
            />
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
    fetchSteps: func,
    steps: any,
    nodeId: any,
};

RunDetailsPipeline.contextTypes = {
    config: object.isRequired,
    params: object,
    pipeline: object,
};

const selectors = createSelector(
    [stepsSelector, logSelector, nodeSelector], (steps, logs, nodeId) => ({ steps, logs, nodeId }));

export default connect(selectors, actions)(RunDetailsPipeline);
