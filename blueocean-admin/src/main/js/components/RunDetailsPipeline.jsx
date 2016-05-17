import React, { Component, PropTypes } from 'react';
import { LogConsole } from '@jenkins-cd/design-language';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';

import LogToolbar from './LogToolbar';

const { string, object, any } = PropTypes;

function uriString(input) {
    return encodeURIComponent(input).replace(/%2F/g, '%252F');
}

export default class RunDetailsPipeline extends Component {
    render() {
        const { pipeline: name, branch, runId } = this.context.params;

        // multibranch special treatment - get url of the log
        const { isMultiBranch } = this.props;
        const baseUrl = '/rest/organizations/jenkins' +
            `/pipelines/${uriString(name)}/`;
        let url;
        let fileName;
        if (isMultiBranch) {
            url = `${baseUrl}/branches/${uriString(branch)}/runs/${runId}/log/`;
            fileName = `${branch}-${runId}.txt`;
        } else {
            url = `${baseUrl}/runs/${runId}/log/`;
            fileName = `${runId}.txt`;
        }

        return (
            <div>
                <ExtensionPoint name="jenkins.pipeline.run.result"
                  pipelineName={name}
                  branchName={isMultiBranch ? branch : undefined}
                  runId={runId}
                />
                <LogToolbar {...{ fileName, url }} />
                <LogConsole {...{ url }} />
            </div>
        );
    }
}

RunDetailsPipeline.propTypes = {
    pipeline: object,
    isMultiBranch: any,
    fileName: string,
    url: string,
};

RunDetailsPipeline.contextTypes = {
    config: object.isRequired,
    params: object,
    pipeline: object,
};
