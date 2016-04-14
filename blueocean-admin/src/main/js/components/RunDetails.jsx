import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    LogConsole,
    fetch,
} from '@jenkins-cd/design-language';

import { PipelineResult } from './pipeResult/Result.jsx';

const { object, array } = PropTypes;

class RunDetails extends Component {
    render() {
        // early out
        if (!this.context.pipeline
            || !this.context.params
            || !this.props.data) {
            return null;
        }

        const {
            context: {
                history,
                location,
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
        const multiBranch = !!branchNames;
        const baseUrl = '/rest/organizations/jenkins' +
            `/pipelines/${name}`;
        let url;
        if (multiBranch) {
            url = `${baseUrl}/branches/${branch}/runs/${runId}/log`;
        } else {
            url = `${baseUrl}/runs/${runId}/log`;
        }
        const result = this.props.data.filter(
            (run) => run.id === runId && run.pipeline === branch)[0];
        result.name = name;
        const afterClose = () => {
            location.pathname = `/pipelines/${name}/activity/`;
            history.replace(location);
        };

        return (<ModalView
          isVisible
          afterClose={afterClose}
          result={result.result}
        >
            <ModalHeader>
                <PipelineResult
                  data={result}
                />
            </ModalHeader>
            <ModalBody>
                <LogConsole url={url} />
            </ModalBody>
        </ModalView>);
    }
}
RunDetails.contextTypes = {
    pipeline: object,
    params: object,
    history: PropTypes.object, // From react-router
    location: PropTypes.object, // From react-router
};

RunDetails.propTypes = {
    data: array,
};

// Decorated for ajax as well as getting pipeline from context
export default fetch(RunDetails, ({ params: { pipeline } }, config) => {
    if (!pipeline) return null;
    const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${pipeline}`;
    return `${baseUrl}/runs`;
});
