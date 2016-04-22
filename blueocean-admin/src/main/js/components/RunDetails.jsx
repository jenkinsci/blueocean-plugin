import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    LogConsole,
    PipelineResult,
    fetch,
} from '@jenkins-cd/design-language';

import LogToolbar from './LogToolbar';

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
                router,
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

        // multibranch special treatment - get url of the log
        const multiBranch = !!branchNames;
        const baseUrl = '/rest/organizations/jenkins' +
            `/pipelines/${name}`;
        let url;
        let fileName = name;
        if (multiBranch) {
            url = `${baseUrl}/branches/${branch}/runs/${runId}/log`;
            fileName = `${branch}-${runId}.txt`;
        } else {
            url = `${baseUrl}/runs/${runId}/log`;
            fileName = `${runId}.txt`;
        }
        const result = this.props.data.filter(
            (run) => run.id === runId && run.pipeline === branch)[0];

        result.name = name;

        const afterClose = () => {
            location.pathname = `/pipelines/${name}/activity/`;
            router.replace(location);
        };

        return (<ModalView
          isVisible
          result={result.result}
          {...{ afterClose }}
        >
            <ModalHeader>
                <PipelineResult data={result} />
            </ModalHeader>
            <ModalBody>
                <div>
                    <LogToolbar {...{ fileName, url }} />
                    <LogConsole {...{ url }} />
                </div>
            </ModalBody>
        </ModalView>);
    }
}

RunDetails.contextTypes = {
    pipeline: object,
    params: object,
    router: object.isRequired, // From react-router
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
