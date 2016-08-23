import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PageTabs,
    Progress,
    TabLink,
} from '@jenkins-cd/design-language';

import {
    actions,
    currentRun as runSelector,
    isMultiBranch as isMultiBranchSelector,
    previous as previousSelector,
    createSelector,
    connect,
} from '../redux';
import { getLocation } from '../util/UrlUtils';
import { RunDetailsHeader } from './RunDetailsHeader';
import { RunRecord } from './records';

const { func, object, any, string } = PropTypes;

class RunDetails extends Component {
    componentWillMount() {
        if (this.context.config && this.context.params) {
            this.props.fetchRun(this.props.params);
            this.opener = this.props.previous;
        }
    }
    navigateToOrganization() {
        const organizationUrl = getLocation({
            organization: this.props.params.organization,
        });
        this.context.router.push(organizationUrl);
    }
    navigateToPipeline() {
        const pipelineUrl = getLocation({
            organization: this.props.params.organization,
            pipeline: this.props.params.pipeline,
        });
        this.context.router.push(pipelineUrl);
    }
    navigateToChanges() {
        const changesUrl = getLocation({
            location: this.context.location,
            pipeline: this.props.pipeline,
            runId: this.props.run.id,
            tab: 'changes',
        });
        this.context.router.push(changesUrl);
    }
    render() {
        // early out
        if (!this.context.params
            || !this.props.run
            || this.props.isMultiBranch === null) {
            return null;
        }

        const { router, location, params } = this.context;

        const baseUrl = getLocation({
            location,
            organization: params.organization,
            pipeline: params.pipeline,
            branch: params.branch,
            runId: params.runId,
        });

        const currentRun = this.props.run;
        
        const runRecord = new RunRecord(currentRun);

        const status = runRecord.getComputedResult() || '';

        const afterClose = () => {
            const url = getLocation({
                location,
                organization: params.organization,
                pipeline: params.pipeline,
            });
            router.push(url);
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
                        {!currentRun.$pending &&
                        <RunDetailsHeader
                          pipeline={this.context.pipeline}
                          data={runRecord}
                          onOrganizationClick={() => this.navigateToOrganization()}
                          onNameClick={() => this.navigateToPipeline()}
                          onAuthorsClick={() => this.navigateToChanges()}
                        />
                        }
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
                        {currentRun.$pending && <Progress />}
                        {currentRun.$success && React.cloneElement(
                            this.props.children,
                            { baseUrl, result: runRecord, ...this.props }
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
    pipeline: object,
};

RunDetails.propTypes = {
    children: PropTypes.node,
    params: any,
    pipeline: object,
    run: object,
    isMultiBranch: any,
    fetchRun: func,
    getPipeline: func,
    previous: string,
};

const selectors = createSelector(
    [runSelector, isMultiBranchSelector, previousSelector],
    (run, isMultiBranch, previous) => ({ run, isMultiBranch, previous }));

export default connect(selectors, actions)(RunDetails);
