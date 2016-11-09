import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import Extensions from '@jenkins-cd/js-extensions';
import {
    ExpandablePath,
    Page,
    PageHeader,
    Title,
    PageTabs,
    TabLink,
    WeatherIcon,
} from '@jenkins-cd/design-language';
import { I18n, AppConfig } from '@jenkins-cd/blueocean-core-js';
import { Icon } from 'react-material-icons-blue';
import {
    actions,
    pipeline as pipelineSelector,
    connect,
    createSelector,
} from '../redux';
import NotFound from './NotFound';
import PageLoading from './PageLoading';
import { buildOrganizationUrl, buildPipelineUrl, buildClassicConfigUrl } from '../util/UrlUtils';
import { documentTitle } from './DocumentTitle';
import compose from '../util/compose';

/**
 * returns true if the pipeline is defined and has branchNames
 */
export function pipelineBranchesUnsupported(pipeline) {
    return (pipeline && !pipeline.branchNames) ||
      (pipeline && !pipeline.branchNames.length);
}

const classicConfigLink = (pipeline) => {
    let link = null;
    if (AppConfig.getInitialUser() !== 'anonymous') {
        link = <a href={buildClassicConfigUrl(pipeline)} target="_blank"><Icon size={24} icon="settings" style={{ fill: '#fff' }} /></a>;
    }
    return link;
};

const translate  = (key) => I18n.t(key, { ns: 'jenkins.plugins.blueocean.dashboard.Messages' });

export class PipelinePage extends Component {
    getChildContext() {
        return {
            pipeline: this.props.pipeline,
        };
    }

    componentWillMount() {
        if (this.props.params) {
            this.props.fetchPipeline(this.props.params.organization, this.props.params.pipeline);
        }
    }

    render() {
        const { pipeline, setTitle } = this.props;
        const { location =  {} } = this.context;
        const { organization, name, fullName, fullDisplayName } = pipeline || {};
        const orgUrl = buildOrganizationUrl(organization);
        const activityUrl = buildPipelineUrl(organization, fullName, 'activity');
        const isReady = pipeline && !pipeline.$pending;

        if (pipeline && pipeline.$failed) {
            return <NotFound />;
        }

        setTitle(`${organization} / ${name}`);

        const baseUrl = buildPipelineUrl(organization, fullName);

        return (
            <Page>
                <PageHeader>
                    {!isReady && <PageLoading duration={2000} />}
                    {!isReady &&
                    <Title>
                        <h1><Link to={orgUrl}>{organization}</Link>
                        <span> / </span></h1>
                    </Title>}
                    {isReady &&
                    <Title>
                        <WeatherIcon score={pipeline.weatherScore} size="large" />
                        <h1>
                            <Link to={orgUrl} query={location.query}>{organization}</Link>
                            <span>&nbsp;/&nbsp;</span>
                            <Link to={activityUrl} query={location.query}>
                                <ExpandablePath path={fullDisplayName} hideFirst className="dark-theme" iconSize={20} />
                            </Link>
                        </h1>
                        <Extensions.Renderer
                          extensionPoint="jenkins.pipeline.detail.header.action"
                          store={this.context.store}
                          pipeline={this.props.pipeline}
                        />
                        {classicConfigLink(pipeline)}
                    </Title>
                    }

                    <PageTabs base={baseUrl}>
                        <TabLink to="/activity">{ translate('pipelinedetail.common.tab.activity') }</TabLink>
                        <TabLink to="/branches">{ translate('pipelinedetail.common.tab.branches') }</TabLink>
                        <TabLink to="/pr">{ translate('pipelinedetail.common.tab.pullrequests') }</TabLink>
                    </PageTabs>
                </PageHeader>
                {isReady && React.cloneElement(this.props.children, { pipeline, setTitle, t: translate, locale: I18n.language })}
            </Page>
        );
    }
}


PipelinePage.propTypes = {
    children: PropTypes.any,
    fetchPipeline: PropTypes.func.isRequired,
    pipeline: PropTypes.any,
    params: PropTypes.object,
    setTitle: PropTypes.func,
};


PipelinePage.contextTypes = {
    config: PropTypes.object.isRequired,
    location: PropTypes.object,
    store: PropTypes.object,
};

PipelinePage.childContextTypes = {
    pipeline: PropTypes.any,
};

const selectors = createSelector([pipelineSelector],
    (pipeline) => ({ pipeline }));

const composed  = compose(
  connect(selectors, actions),
  documentTitle
);

export default composed(PipelinePage);
