import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    LogConsole,
    PipelineResult,
} from '@jenkins-cd/design-language';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';

import LogToolbar from './LogToolbar';
import {
    actions,
    currentRuns as runsSelector,
    isMultiBranch as isMultiBranchSelector,
    createSelector,
    connect,
} from '../redux';

const { func, object, array, any } = PropTypes;

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
            || this.props.isMultiBranch === null) {
            return null;
        }
        const {
            router,
            pipeline,
            params,
            } = this.context;

        const name = pipeline.name;
        const { branch, runId } = params; // From route

        // multibranch special treatment - get url of the log
        const multiBranch = !!pipeline.branchNames;
        const baseUrl = '/rest/organizations/jenkins' +
            `/pipelines/${uriString(name)}/`;
        let url;
        let fileName = name;
        if (this.props.isMultiBranch) {
            url = `${baseUrl}/branches/${uriString(branch)}/runs/${runId}/log/`;
            fileName = `${branch}-${runId}.txt`;
        } else {
            url = `${baseUrl}/runs/${runId}/log/`;
            fileName = `${runId}.txt`;
        }
        const result = this.props.runs.filter(
            (run) => run.id === runId && decodeURIComponent(run.pipeline) === branch)[0];

        result.name = name;

        const afterClose = () => {
            router.goBack();
        };

        return (
            <ModalView
              isVisible
              result={result.result}
              {...{ afterClose }}
            >
                <ModalHeader>
                    <PipelineResult data={result} />
                </ModalHeader>
                <ModalBody>
                    <div>
                        <ExtensionPoint name="jenkins.pipeline.run.result"
                          pipelineName={name}
                          branchName={multiBranch ? branch : undefined}
                          runId={runId}
                        />
                        <LogToolbar {...{ fileName, url }} />
                        <LogConsole {...{ url }} />
                    </div>
                </ModalBody>
            </ModalView>
        );
    }
}

RunDetails.contextTypes = {
    config: object.isRequired,
    params: object,
    router: object.isRequired, // From react-router
};

RunDetails.propTypes = {
    runs: array,
    isMultiBranch: any,
    fetchIfNeeded: func,
    fetchRunsIfNeeded: func,
    setPipeline: func,
    getPipeline: func,
};

const selectors = createSelector(
    [runsSelector, isMultiBranchSelector],
    (runs, isMultiBranch) => ({ runs, isMultiBranch }));

export default connect(selectors, actions)(RunDetails);
