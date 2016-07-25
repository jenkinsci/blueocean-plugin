import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PipelineResult,
    PageTabs,
} from '@jenkins-cd/design-language';
import Extensions, { dataType } from '@jenkins-cd/js-extensions';

import {
    actions,
    currentRuns as runsSelector,
    isMultiBranch as isMultiBranchSelector,
    previous as previousSelector,
    createSelector,
    connect,
} from '../redux';

import {
    buildOrganizationUrl,
    buildPipelineUrl,
    buildRunDetailsUrl,
} from '../util/UrlUtils';

const { func, object, array, any, string } = PropTypes;

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
            this.opener = this.props.previous;
        }
    }
    navigateToOrganization() {
        const { organization } = this.props.pipeline;
        const organizationUrl = buildOrganizationUrl(organization);
        this.context.router.push(organizationUrl);
    }
    navigateToPipeline() {
        const { organization, fullName } = this.props.pipeline;
        const pipelineUrl = buildPipelineUrl(organization, fullName);
        this.context.router.push(pipelineUrl);
    }
    navigateToChanges() {
        const {
            params: {
                organization,
                pipeline,
                branch,
                runId,
            },
        } = this.context;

        const changesUrl = buildRunDetailsUrl(organization, pipeline, branch, runId, 'changes');
        this.context.router.push(changesUrl);
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
            location,
            params: {
                organization,
                branch,
                runId,
                pipeline: name,
            },
        } = this.context;

        const baseUrl = buildRunDetailsUrl(organization, name, branch, runId);

        /* eslint-disable arrow-body-style */
        const currentRun = this.props.runs.filter((run) => {
            return run.id === runId &&
                decodeURIComponent(run.pipeline) === branch;
        })[0];

        currentRun.name = name;

        const status = currentRun.result === 'UNKNOWN' ? currentRun.state : currentRun.result;

        const afterClose = () => {
            const fallbackUrl = buildPipelineUrl(organization, name);

            location.pathname = this.opener || fallbackUrl;

            router.push(location);
        };

        return (
            <ModalView
              isVisible
              transitionClass="expand-in"
              transitionDuration={150}
              result={status}
              {...{ afterClose }}
            >
                <ModalHeader>
                    <div>
                        <PipelineResult data={currentRun}
                          onOrganizationClick={() => this.navigateToOrganization()}
                          onNameClick={() => this.navigateToPipeline()}
                          onAuthorsClick={() => this.navigateToChanges()}
                        />
                        <PageTabs base={baseUrl}>
                            <Extensions.Renderer extensionPoint="rundetails.main.navigation" filter={dataType(currentRun)} currentRun={currentRun} baseLink={baseUrl} />
                        </PageTabs>
                    </div>
                </ModalHeader>
                <ModalBody>
                    <div>
                        {React.cloneElement(
                            this.props.children,
                            { baseUrl, result: currentRun, ...this.props }
                        )}
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
    location: object.isRequired, // From react-router
};

RunDetails.propTypes = {
    children: PropTypes.node,
    pipeline: object,
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
