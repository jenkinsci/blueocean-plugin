import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PageTabs,
    TabLink,
} from '@jenkins-cd/design-language';
import { ReplayButton, RunButton, I18n } from '@jenkins-cd/blueocean-core-js';
import { translate } from 'react-i18next';

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

import compose from '../util/compose';

import { RunDetailsHeader } from './RunDetailsHeader';
import { RunRecord } from './records';
import PageLoading from './PageLoading';
import { AppConfig } from '@jenkins-cd/blueocean-core-js';

const { func, object, any, string } = PropTypes;

const classicConfigLink = (pipeline) => {
    let link = null;
    if (AppConfig.getInitialUser() !== 'anonymous') {
        link = <a href={buildClassicConfigUrl(pipeline)} target="_blank"><Icon size={24} icon="settings" style={{ fill: '#fff' }} /></a>;
    }
    return link;
};


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
        const { location } = this.context;
        const organizationUrl = buildOrganizationUrl(organization);
        location.pathname = organizationUrl;
        this.context.router.push(location);
    }
    navigateToPipeline() {
        const { organization, fullName } = this.props.pipeline;
        const { location } = this.context;
        const pipelineUrl = buildPipelineUrl(organization, fullName);
        location.pathname = pipelineUrl;
        this.context.router.push(location);
    }
    navigateToChanges() {
        const {
            location,
            params: {
                organization,
                pipeline,
                branch,
                runId,
            },
        } = this.context;

        const changesUrl = buildRunDetailsUrl(organization, pipeline, branch, runId, 'changes');
        location.pathname = changesUrl;
        this.context.router.push(location);
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

        const { run, setTitle, t, locale } = this.props;
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
                          t={ t }
                          locale={locale}
                          pipeline={pipeline}
                          data={currentRun}
                          onOrganizationClick={() => this.navigateToOrganization()}
                          onNameClick={() => this.navigateToPipeline()}
                          onAuthorsClick={() => this.navigateToChanges()}
                        />
                        <PageTabs base={baseUrl}>
                            <TabLink to="/pipeline">{t('Pipeline')}</TabLink>
                            <TabLink to="/changes">{t('Changes')}</TabLink>
                            <TabLink to="/tests">{t('Tests')}</TabLink>
                            <TabLink to="/artifacts">{t('Artifacts')}</TabLink>
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
                        {run.$success && React.cloneElement(
                            this.props.children,
                            { locale: I18n.language, baseUrl, result: currentRun, ...this.props }
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
    t: func,
};

const selectors = createSelector(
    [runSelector, isMultiBranchSelector, previousSelector],
    (run, isMultiBranch, previous) => ({ run, isMultiBranch, previous }));

const composed  = compose(
  translate(['jenkins.plugins.blueocean.dashboard.Messages'], { wait: true }),
  connect(selectors, actions)
);

export default composed(RunDetails);
