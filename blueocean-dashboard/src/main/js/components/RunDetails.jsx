import React, { Component, PropTypes } from 'react';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PageTabs,
    TabLink,
} from '@jenkins-cd/design-language';
import { i18nTranslator, ReplayButton, RunButton } from '@jenkins-cd/blueocean-core-js';

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
import { Paths, capable, locationService } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { User } from '@jenkins-cd/blueocean-core-js';

const { func, object, any, string } = PropTypes;

const { rest: RestPaths } = Paths;

const classicConfigLink = (pipeline) => {
    let link = null;
    if (!User.current().isAnonymous()) {
        let url = buildClassicConfigUrl(pipeline);
        link = (
            <a href={url} target="_blank" style={{ height: '24px' }}>
                <Icon size={24} icon="settings" style={{ fill: '#fff' }} />
            </a>
        );
    }
    return link;
};

const translate = i18nTranslator('blueocean-dashboard');


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
                this.opener = locationService.previous;
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
        const run = this.context.activityService.getActivity(this.href);
        // early out
        if (!this.context.params
            || !run) {
            return null;
        }


        const { router, location, params } = this.context;
        const { pipeline, setTitle, t, locale } = this.props;

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
                          t={ t }
                          locale={locale}
                          pipeline={pipeline}
                          data={currentRun}
                          onOrganizationClick={() => this.navigateToOrganization()}
                          onNameClick={() => this.navigateToPipeline()}
                          onAuthorsClick={() => this.navigateToChanges()}
                        />
                        <PageTabs base={baseUrl}>
                            <TabLink to="/pipeline">{t('rundetail.header.tab.pipeline', {
                                defaultValue: 'Pipeline',
                            })}</TabLink>
                            <TabLink to="/changes">{t('rundetail.header.tab.changes', {
                                defaultValue: 'Changes',
                            })}</TabLink>
                            <TabLink to="/tests">{t('rundetail.header.tab.tests', {
                                defaultValue: 'Tests',
                            })}</TabLink>
                            <TabLink to="/artifacts">{t('rundetail.header.tab.artifacts', {
                                defaultValue: 'Artifacts',
                            })}</TabLink>
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
                            { locale: translate.lng, baseUrl, t: translate, result: currentRun, isMultiBranch: this.isMultiBranch, ...this.props }
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
    locale: string,
    t: func,
};

export default RunDetails;

