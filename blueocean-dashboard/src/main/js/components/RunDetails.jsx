import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PageTabs,
    TabLink,
} from '@jenkins-cd/design-language';

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

import { RunDetailsHeader } from './RunDetailsHeader';
import { RunRecord } from './records';

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

        const { router, location, params } = this.context;
        
        const baseUrl = buildRunDetailsUrl(params.organization, params.pipeline, params.branch, params.runId);
          
        const foundRun = this.props.runs.find((run) =>
            run.id === params.runId &&
                decodeURIComponent(run.pipeline) === params.branch
        );
       // deep-linking across RunDetails for different pipelines yields 'runs' data for the wrong pipeline
        // during initial render. when runs are refetched the screen will render again with 'currentRun' correctly set
        if (!foundRun) {
            return null;
        }

        const currentRun = new RunRecord(foundRun);
    
        const status = currentRun.getComputedResult();
       
        const afterClose = () => {
            const fallbackUrl = buildPipelineUrl(params.organization, params.pipeline);
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
                        <RunDetailsHeader data={currentRun}
                          onOrganizationClick={() => this.navigateToOrganization()}
                          onNameClick={() => this.navigateToPipeline()}
                          onAuthorsClick={() => this.navigateToChanges()}
                        />
                        <PageTabs base={baseUrl}>
                            <TabLink to="/pipeline">Pipeline</TabLink>
                            <TabLink to="/changes">Changes</TabLink>
                            <TabLink to="/tests">Tests</TabLink>
                            <TabLink to="/artifacts">Artifacts</TabLink>
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
