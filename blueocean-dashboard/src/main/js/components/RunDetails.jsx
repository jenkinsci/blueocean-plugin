import React, { Component, PropTypes } from 'react';
import { TabLink } from '@jenkins-cd/design-language';
import { i18nTranslator, ReplayButton, RunButton, LoginButton, logging } from '@jenkins-cd/blueocean-core-js';
import Extensions, { dataType } from '@jenkins-cd/js-extensions';

import { Icon } from '@jenkins-cd/design-language';

import { UrlBuilder } from '@jenkins-cd/blueocean-core-js';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { RunDetailsHeader } from './RunDetailsHeader';
import { RunRecord } from './records';
import { FullScreen } from './FullScreen';
import { Paths, capable, Security } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';

const { rest: RestPaths } = Paths;
const logger = logging.logger('io.jenkins.blueocean.dashboard.RunDetails');

const translate = i18nTranslator('blueocean-dashboard');
const webTranslate = i18nTranslator('blueocean-web');

const classicConfigLink = pipeline => {
    let link = null;
    if (Security.permit(pipeline).configure()) {
        let url = UrlBuilder.buildClassicConfigUrl(pipeline);
        link = (
            <a href={url} target="_blank" title={webTranslate('toast.configure', { defaultValue: 'Configure' })} style={{ height: '24px' }}>
                <Icon size={24} icon="ActionSettings" />
            </a>
        );
    }
    return link;
};

const classicJobRunLink = (pipeline, branch, runId) => {
    let runUrl;
    if (pipeline.branchNames) {
        runUrl = `${UrlBuilder.classicJobRoot(pipeline.fullName)}/job/${encodeURIComponent(branch)}/${encodeURIComponent(runId)}`;
    } else {
        runUrl = `${UrlBuilder.classicJobRoot(pipeline.fullName)}/${encodeURIComponent(runId)}`;
    }
    return (
        <a className="rundetails_exit_to_app" href={runUrl} style={{ height: '24px' }} title={webTranslate('go.to.classic', { defaultValue: 'Go to classic' })}>
            <Icon size={24} icon="ActionExitToApp" />
        </a>
    );
};

function getTestSummaryUrl(runDetails) {
    if (runDetails && runDetails._links.blueTestSummary) {
        return runDetails._links.blueTestSummary.href;
    }
    return null;
}

@observer
class RunDetails extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: true, actions: [] };
    }

    componentWillMount() {
        this._fetchRun(this.props);
        Extensions.store.getExtensions(['jenkins.run.actions'], Extensions.Utils.sortByOrdinal, (actions = []) => {
            this.setState({ actions });
        });
    }

    componentWillReceiveProps(nextProps) {
        if (
            nextProps.pipeline !== this.props.pipeline ||
            nextProps.params !== this.props.params ||
            nextProps.params.organization !== this.props.params.organization ||
            nextProps.params.pipeline !== this.props.params.pipeline ||
            nextProps.params.branch !== this.props.params.branch ||
            nextProps.params.runId !== this.props.params.runId
        ) {
            this._fetchRun(nextProps);
        }
    }

    _fetchRun(props) {
        this.isMultiBranch = capable(props.pipeline, MULTIBRANCH_PIPELINE);

        if (this.context.config && this.context.params) {
            this.href = RestPaths.run({
                organization: props.params.organization,
                pipeline: props.params.pipeline,
                branch: this.isMultiBranch && props.params.branch,
                runId: props.params.runId,
            });

            this.context.activityService.fetchActivity(this.href, { useCache: true }).then(run => {
                const testSummaryUrl = getTestSummaryUrl(run);
                return testSummaryUrl && this.context.activityService.fetchTestSummary(testSummaryUrl);
            });
        }
    }

    navigateToOrganization = () => {
        const { organization } = this.props.pipeline;
        const { location } = this.context;
        const organizationUrl = UrlBuilder.buildOrganizationUrl(organization);
        location.pathname = organizationUrl;
        this.context.router.push(location);
    };

    navigateToPipeline = () => {
        const { organization, fullName } = this.props.pipeline;
        const { location } = this.context;
        const pipelineUrl = UrlBuilder.buildPipelineUrl(organization, fullName);
        location.pathname = pipelineUrl;
        this.context.router.push(location);
    };

    navigateToChanges = () => {
        const {
            location,
            params: { organization, pipeline, branch, runId },
        } = this.context;

        const changesUrl = UrlBuilder.buildRunUrl(organization, pipeline, branch, runId, 'changes');
        location.pathname = changesUrl;
        this.context.router.push(location);
    };

    closeButtonClicked = () => {
        this.setState({ isVisible: false });
    };

    afterClose = () => {
        const { router, params } = this.context;
        router.push(UrlBuilder.buildPipelineUrl(params.organization, params.pipeline));
    };

    render() {
        const activityService = this.context.activityService;
        const run = activityService.getActivity(this.href);
        const testSummaryUrl = run && getTestSummaryUrl(run);
        const testSummary = testSummaryUrl && activityService.getTestSummary(testSummaryUrl);

        // early out
        if (!this.context.params || !run) {
            return null;
        }

        const { router, location, params } = this.context;
        const { pipeline, setTitle, t, locale } = this.props;
        const { isVisible, actions } = this.state;

        if (!run || !pipeline) {
            this.props.setTitle(translate('common.pager.loading', { defaultValue: 'Loading...' }));
            return null;
        }

        const baseUrl = UrlBuilder.buildRunUrl(params.organization, params.pipeline, params.branch, params.runId, null);
        logger.debug('params', params.organization, params.pipeline, params.branch, params.runId);
        const currentRun = new RunRecord(run);
        const computedTitle = `${currentRun.organization} / ${pipeline.fullName} / ${params.pipeline === params.branch ? '' : `${params.branch} / `} #${
            currentRun.id
        }`;
        setTitle(computedTitle);

        const switchRunDetails = newUrl => {
            location.pathname = newUrl;
            router.push(location);
        };

        const base = { base: baseUrl };

        const tabs = [
            <TabLink to="/pipeline" {...base}>
                {t('rundetail.header.tab.pipeline', {
                    defaultValue: 'Pipeline',
                })}
            </TabLink>,
        ];

        actions.map(descriptor => {
            const action = new descriptor({pipeline, run: currentRun});
            const badge = action.notification && <div className="TabBadgeIcon">{ action.notification }</div>;
            tabs.push(
                <TabLink to={'/' + action.name} { ...base }>{action.title}{badge}</TabLink>
            );
        });

        const iconButtons = [
            <ReplayButton className="icon-button dark" runnable={this.props.pipeline} latestRun={currentRun} onNavigation={switchRunDetails} autoNavigate />,
            <RunButton className="icon-button dark" runnable={this.props.pipeline} latestRun={currentRun} buttonType="stop-only" />,
            <Extensions.Renderer
                extensionPoint="jenkins.blueocean.rundetails.top.widgets"
                filter={dataType(currentRun)}
                pipeline={pipeline}
                run={currentRun}
                back={() => this.navigateToPipeline()}
            />,
            classicConfigLink(pipeline),
            classicJobRunLink(pipeline, params.branch, params.runId),
            <LoginButton className="user-component button-bar layout-small inverse" translate={webTranslate} />,
        ];

        return (
            <FullScreen isVisible={isVisible} afterClose={this.afterClose} onDismiss={this.closeButtonClicked}>
                <RunDetailsHeader
                    t={t}
                    locale={locale}
                    pipeline={pipeline}
                    data={currentRun}
                    runButton={iconButtons}
                    topNavLinks={tabs}
                    onOrganizationClick={this.navigateToOrganization}
                    onNameClick={this.navigateToPipeline}
                    onAuthorsClick={this.navigateToChanges}
                    onCloseClick={this.closeButtonClicked}
                    isMultiBranch={this.isMultiBranch}
                />

                <div className="RunDetails-content">
                    {run &&
                        React.cloneElement(this.props.children, {
                            baseUrl,
                            testSummary,
                            locale: translate.lng,
                            t: translate,
                            result: currentRun,
                            isMultiBranch: this.isMultiBranch,
                            ...this.props,
                        })}
                </div>
            </FullScreen>
        );
    }
}

RunDetails.contextTypes = {
    config: PropTypes.object.isRequired,
    params: PropTypes.object,
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object.isRequired, // From react-router
    activityService: PropTypes.object.isRequired,
};

RunDetails.propTypes = {
    children: PropTypes.node,
    params: PropTypes.any,
    pipeline: PropTypes.object,
    run: PropTypes.object,
    previous: PropTypes.string,
    setTitle: PropTypes.func,
    locale: PropTypes.string,
    t: PropTypes.func,
};

export default RunDetails;
