import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    LogConsole,
    PipelineResult,
} from '@jenkins-cd/design-language';

import LogToolbar from './LogToolbar';
import {
    actions,
    currentRuns as runsSelector,
    isMultiBranch as isMultiBranchSelector,
    previous as previousSelector,
    createSelector,
    connect,
} from '../redux';

const { func, object, array, any, string } = PropTypes;

function uriString(input) {
    return encodeURIComponent(input).replace(/%2F/g, '%252F');
}

class RunDetails extends Component {
    componentWillMount() {
        if (this.context.config && this.context.params) {
            const {
                params: {
                    pipeline,
                },
                config = {},
            } = this.context;
            config.pipeline = pipeline;
            this.props.fetchRunsIfNeeded(config);
            this.props.setPipeline(config);
        }
    }
    render() {
        // early out
        if (!this.context.params
            || !this.props.runs
            || this.props.isMultiBranch === null
        ) {
            return null;
        }
        const {
            context: {
                router,
                location,
                params: {
                    branch,
                    runId,
                    pipeline: name,
                },
            },
            props: {
                previous,
            },
        } = this;

        // multibranch special treatment - get url of the log
        const baseUrl = '/rest/organizations/jenkins' +
            `/pipelines/${name}`;
        let url;
        let fileName = name;
        if (this.props.isMultiBranch) {
            url = `${baseUrl}/branches/${uriString(branch)}/runs/${runId}/log`;
            fileName = `${branch}-${runId}.txt`;
        } else {
            url = `${baseUrl}/runs/${runId}/log`;
            fileName = `${runId}.txt`;
        }
        const result = this.props.runs.filter(
            (run) => run.id === runId && decodeURIComponent(run.pipeline) === branch)[0];

        result.name = name;

        const afterClose = () => {
            location.hash = `#${branch}-${runId}`;
            if (previous) {
                location.pathname = previous;
            } else {
                location.pathname = `/pipelines/${name}/activity/`;
            }
            router.push(location);
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
    config: object.isRequired,
    params: object,
    router: object.isRequired, // From react-router
    location: object.isRequired, // From react-router
};

RunDetails.propTypes = {
    runs: array,
    isMultiBranch: any,
    fetchIfNeeded: func,
    fetchRunsIfNeeded: func,
    setPipeline: func,
    getPipeline: func,
    previous: string,
};

const selectors = createSelector(
    [runsSelector, isMultiBranchSelector, previousSelector],
    (runs, isMultiBranch, previous) => ({ runs, isMultiBranch, previous }));

export default connect(selectors, actions)(RunDetails);
