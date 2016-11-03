import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PageTabs,
    TabLink,
} from '@jenkins-cd/design-language';

import { ReplayButton, RunButton } from '@jenkins-cd/blueocean-core-js';

import { Icon } from 'react-material-icons-blue';

import {
    actions,
    currentRun as runSelector,
    isMultiBranch as isMultiBranchSelector,
    previous as previousSelector,
    createSelector,
    connect,
} from '../redux';

import {
    buildOrganizationUrl,
    buildPipelineUrl,
    buildRunDetailsUrl,
    buildClassicConfigUrl,
} from '../util/UrlUtils';

import { RunDetailsHeader } from './RunDetailsHeader';
import { RunRecord } from './records';
import PageLoading from './PageLoading';

const { func, object, any, string } = PropTypes;

class RunDetails extends Component {

    componentWillMount() {
        this._fetchRun(this.props, true);
    }

    componentWillReceiveProps(nextProps) {
        if (!this._didRunChange(this.props.params, nextProps.params)) {
            return;
        }

        // in some cases the route params might have actually changed (such as 'runId' during a Re-run) so re-fetch
        // also don't update the 'previous route' otherwise closing the modal will try to navigate back to last run
        this._fetchRun(nextProps, false);
    }

    _fetchRun(props, storePreviousRoute) {
        if (props.isMultiBranch === null) {
            return; // multiple redux selectors haven't completed
        }
        if (this.context.config && this.context.params) {
            props.fetchRun({
                organization: props.params.organization,
                pipeline: props.params.pipeline,
                branch: props.isMultiBranch && props.params.branch,
                runId: props.params.runId,
            });

            if (storePreviousRoute) {
                this.opener = props.previous;
            }
        }
    }

    _didRunChange(oldParams, newParams) {
        return oldParams.organization !== newParams.organization ||
                oldParams.pipeline !== newParams.pipeline ||
                oldParams.branch !== newParams.branch ||
                oldParams.runId !== newParams.runId;
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
            || !this.props.run
            || this.props.isMultiBranch === null) {
            return null;
        }

        if (this.props.run.$pending || this.context.pipeline.$pending) {
            return <PageLoading />;
        }

        const { router, location, params, pipeline = {} } = this.context;

        const baseUrl = buildRunDetailsUrl(params.organization, params.pipeline, params.branch, params.runId);

        const { run, setTitle } = this.props;
        const currentRun = new RunRecord(run);
        const status = currentRun.getComputedResult() || '';

        const switchRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        setTitle(`${currentRun.organization} / ${pipeline.fullName} #${currentRun.id}`);

        const afterClose = () => {
            const fallbackUrl = buildPipelineUrl(params.organization, params.pipeline);
            location.pathname = this.opener || fallbackUrl;
            // reset query
            /*
            FIXME: reset query when you go back, we may want to store the whole location object in previous so we have a perfect prev.
            this.opener would then be location and we the above location = this.opener || {pathname: fallbackUrl]
             */
            location.query = null;
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
                        <RunDetailsHeader
                          pipeline={pipeline}
                          data={currentRun}
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

                        <div className="button-bar">
                            <ReplayButton
                              className="dark"
                              runnable={this.props.pipeline}
                              latestRun={currentRun}
                              onNavigation={switchRunDetails}
                              autoNavigate
                            />

                            <RunButton
                              className="dark"
                              runnable={this.props.pipeline}
                              latestRun={currentRun}
                              buttonType="stop-only"
                            />

                            <a href={buildClassicConfigUrl(window, pipeline)} target="_blank"><Icon size={24} icon="settings" style={{ fill: '#fff' }} /></a>

                        </div>
                    </div>
                </ModalHeader>
                <ModalBody>
                    <div>
                        {run.$success && React.cloneElement(
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
    setTitle: func,
};

const selectors = createSelector(
    [runSelector, isMultiBranchSelector, previousSelector],
    (run, isMultiBranch, previous) => ({ run, isMultiBranch, previous }));


export default connect(selectors, actions)(RunDetails);
