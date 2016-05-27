import React, { Component, PropTypes } from 'react';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';
import Nodes from './Nodes';
import {
    nodes as nodeSelector,
    logs as logSelector,
    actions,
    calculateRunLogURLObject,
    calculateNodeBaseUrl,
    connect,
    createSelector,
} from '../redux';

import LogToolbar from './LogToolbar';

const { string, object, any, func } = PropTypes;

export class RunDetailsPipeline extends Component {
    componentWillMount() {
        if (this.context.config && this.context.params) {
            if (this.props.fetchNodes) {
                this.props.fetchNodes(this.generateConfig());
            }
        }
    }

    generateConfig() {
        const {
      params: {
        pipeline: name, branch, runId,
        },
      config = {},
      } = this.context;
        const { isMultiBranch } = this.props;
        const mergedConfig = { ...config, name, branch, runId, isMultiBranch };
        return mergedConfig;
    }
  
    render() {
        const { pipeline: name, branch, runId } = this.context.params;
        const { isMultiBranch, nodes } = this.props;

        const mergedConfig = this.generateConfig();
        const key = calculateNodeBaseUrl(mergedConfig);
        const logGeneral = calculateRunLogURLObject(mergedConfig);
        return (
      <div>
        <ExtensionPoint name="jenkins.pipeline.run.result"
          pipelineName={name}
          branchName={isMultiBranch ? branch : undefined}
          runId={runId}
        />
        <LogToolbar fileName={logGeneral.fileName} url={logGeneral.url} />
        { nodes && <Nodes
          nodeInformation={nodes[key]}
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
    nodes: any,
};

RunDetailsPipeline.contextTypes = {
    config: object.isRequired,
    params: object,
    pipeline: object,
};

const selectors = createSelector([nodeSelector, logSelector], (nodes, logs) => ({ nodes, logs }));

export default connect(selectors, actions)(RunDetailsPipeline);
