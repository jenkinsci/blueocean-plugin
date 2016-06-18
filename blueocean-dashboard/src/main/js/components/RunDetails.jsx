import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PipelineResult,
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
            this.props.setPipeline(config);
            this.opener = this.props.previous;
        }
    }
    navigateToOrganization() {
        const { organization } = this.props.pipeline;
        const organizationUrl = `/organizations/${organization}`;
        this.context.router.push(organizationUrl);
    }
    navigateToPipeline() {
        const { organization, name } = this.props.pipeline;
        const pipelineUrl = `/organizations/${organization}/${name}`;
        this.context.router.push(pipelineUrl);
    }
    navigateToChanges() {
        const {
            params: {
                organization,
                pipeline: name,
                branch,
                runId,
            },
        } = this.context;

        const changesUrl = `/organizations/${organization}/${name}` +
            `/detail/${branch}/${runId}/changes`;
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

        const baseUrl = `/organizations/${organization}/${name}` +
            `/detail/${branch}/${runId}`;

        const currentRun = this.props.runs.filter(
            (run) => run.id === runId && decodeURIComponent(run.pipeline) === branch)[0];

        currentRun.name = name;

        const status = currentRun.result === 'UNKNOWN' ? currentRun.state : currentRun.result;

        const afterClose = () => {
            const fallback = `/organizations/${organization}/${name}/`;

            location.pathname = this.opener || fallback;
            location.hash = `#${branch}-${runId}`;

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
