import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    LogConsole,
    PipelineResult,
    PageTabs,
    TabLink,
    fetch,
} from '@jenkins-cd/design-language';

import LogToolbar from './LogToolbar';

const { object, array } = PropTypes;

function uriString(input) {
    return encodeURIComponent(input).replace(/%2F/g, '%252F');
}

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

        // TODO: need the correct way to establish the base url
        const baseUrl = this.props.location.pathname;

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
        const result = this.props.data.filter(
            (run) => run.id === runId && decodeURIComponent(run.pipeline) === branch)[0];

        result.name = name;

        const afterClose = () => {
            router.goBack();
        };

        return (<ModalView
          isVisible
          result={result.result}
          {...{ afterClose }}
        >
            <ModalHeader>
                <div>
                    <PipelineResult data={result} />
                    <PageTabs base={baseUrl}>
                        <TabLink to="/">Pipeline</TabLink>
                        <TabLink to="changes">Changes</TabLink>
                        <TabLink to="tests">Tests</TabLink>
                        <TabLink to="artifacts">Artifacts</TabLink>
                    </PageTabs>
                    </div>
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
