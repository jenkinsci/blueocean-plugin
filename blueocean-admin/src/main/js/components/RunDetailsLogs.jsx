import React, { Component, PropTypes } from 'react';
import {
    LogConsole,
} from '@jenkins-cd/design-language';

import LogToolbar from './LogToolbar';

const { string, object } = PropTypes;

function uriString(input) {
    return encodeURIComponent(input).replace(/%2F/g, '%252F');
}

export default class RunDetailsLogs extends Component {
    render() {
        const {
            context: {
                pipeline: {
                    branchNames,
                    name,
                },
                params: {
                    branch,
                    runId,
                },
            },
        } = this;

        // multibranch special treatment - get url of the log
        const multiBranch = !!branchNames;
        const restBaseUrl = '/rest/organizations/jenkins' +
            `/pipelines/${uriString(name)}`;
        let url;
        let fileName = name;
        if (multiBranch) {
            url = `${restBaseUrl}/branches/${uriString(branch)}/runs/${runId}/log`;
            fileName = `${branch}-${runId}.txt`;
        } else {
            url = `${restBaseUrl}/runs/${runId}/log`;
            fileName = `${runId}.txt`;
        }

        return (
            <div>
                <LogToolbar {...{ fileName, url }} />
                <LogConsole {...{ url }} />
            </div>
        );
    }
}

RunDetailsLogs.propTypes = {
    pipeline: object,
    fileName: string,
    url: string,
};

RunDetailsLogs.contextTypes = {
    config: object.isRequired,
    params: object,
    router: object.isRequired, // From react-router
    pipeline: object,
};
