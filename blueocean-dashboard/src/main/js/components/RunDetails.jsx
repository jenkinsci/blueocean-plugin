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
    buildOrganizationUrl,
    buildPipelineUrl,
    buildRunDetailsUrl,
    buildClassicConfigUrl,
} from '../util/UrlUtils';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { RunDetailsHeader } from './RunDetailsHeader';
import { RunRecord } from './records';
import PageLoading from './PageLoading';
import { Paths, capable } from '@jenkins-cd/blueocean-core-js';
import { AppConfig } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
const { func, object, any, string } = PropTypes;

const { rest: RestPaths } = Paths;

const classicConfigLink = (pipeline) => {
    let link = null;
    if (AppConfig.getInitialUser() !== 'anonymous') {
        let url = buildClassicConfigUrl(pipeline);
        link = (
            <a href={url} target="_blank" style={{ height: '24px' }}>
                <Icon size={24} icon="settings" style={{ fill: '#fff' }} />
            </a>
        );
    }
    return link;
};

@observer
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
        this.isMultiBranch = capable(this.props.pipeline, MULTIBRANCH_PIPELINE);
       
        if (this.context.config && this.context.params) {
            this.href = RestPaths.run({
                organization: props.params.organization,
                pipeline: props.params.pipeline,
                branch: this.isMultiBranch && props.params.branch,
                runId: props.params.runId,
            });
            
            this.context.activityService.fetchActivity(this.href, { useCache: true });

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
        const run = this.context.activityService.getActivity(this.href);
        // early out
        if (!this.context.params
            || !run) {
            return null;
        }

        
        const { router, location, params } = this.context;
        const { pipeline, setTitle } = this.props;

        if (!run || !pipeline) {
            return <PageLoading />;
        }

        const baseUrl = buildRunDetailsUrl(params.organization, params.pipeline, params.branch, params.runId);

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
                            {classicConfigLink(pipeline)}
                        </div>
                    </div>
                </ModalHeader>
                <ModalBody>
                    <div>
                        {run && React.cloneElement(
                            this.props.children,
                            { baseUrl, result: currentRun, isMultiBranch: this.isMultiBranch, ...this.props }
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
    activityService: object.isRequired,
};

RunDetails.propTypes = {
    children: PropTypes.node,
    params: any,
    pipeline: object,
    run: object,
    previous: string,
    setTitle: func,
};


export default RunDetails;
