import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import Extensions from '@jenkins-cd/js-extensions';
import {
    ExpandablePath,
    Page,
    TabLink,
    WeatherIcon,
} from '@jenkins-cd/design-language';
import { i18nTranslator, NotFound, Paths, ContentPageHeader, logging, AppConfig, Security } from '@jenkins-cd/blueocean-core-js';
import { Icon } from '@jenkins-cd/react-material-icons';
import { buildOrganizationUrl, buildPipelineUrl, buildClassicConfigUrl } from '../util/UrlUtils';
import { documentTitle } from './DocumentTitle';
import { observer } from 'mobx-react';
import { observable, action } from 'mobx';

const logger = logging.logger('io.jenkins.blueocean.dashboard.PipelinePage');

const RestPaths = Paths.rest;

const classicConfigLink = (pipeline) => {
    let link = null;
    if (Security.permit(pipeline).configure()) {
        link = <a href={buildClassicConfigUrl(pipeline)} target="_blank"><Icon size={24} icon="settings" style={{ fill: '#fff' }} /></a>;
    }
    return link;
};

const translate = i18nTranslator('blueocean-dashboard');

@observer
export class PipelinePage extends Component {


    componentWillMount() {
        if (this.props.params) {
            this.href = RestPaths.pipeline(this.props.params.organization, this.props.params.pipeline);
            this.context.pipelineService.fetchPipeline(this.href, { useCache: true }).catch(err => this._setError(err));
        }
    }

    @observable error;

    @action
    _setError(error) {
        this.error = error;
    }


    render() {
        const pipeline = this.context.pipelineService.getPipeline(this.href);

        const { setTitle } = this.props;
        const { location = {} } = this.context;

        const { organization, name, fullName, fullDisplayName } = pipeline || {};
        
        const organizationName = organization || AppConfig.getOrganizationName();
        const organizationDisplayName = organization === AppConfig.getOrganizationName() ? AppConfig.getOrganizationDisplayName() : organization;
        
        const orgUrl = buildOrganizationUrl(organizationName);
        const activityUrl = buildPipelineUrl(organizationName, fullName, 'activity');
        const isReady = !!pipeline;

        if (!pipeline && this.error) {
            logger.log(`Error finding pipeline page for ${fullName}.`, this.error);
            return <NotFound />;
        }

        if (isReady) {
            setTitle(`${organizationDisplayName} / ${name}`);
        } else {
            setTitle(translate('common.pager.loading', { defaultValue: 'Loading...' }));
        }

        const baseUrl = buildPipelineUrl(organizationName, fullName);

        const pageTabLinks = [
            <TabLink to="/activity">{ translate('pipelinedetail.common.tab.activity', { defaultValue: 'Activity' }) }</TabLink>,
            <TabLink to="/branches">{ translate('pipelinedetail.common.tab.branches', { defaultValue: 'Branches' }) }</TabLink>,
            <TabLink to="/pr">{ translate('pipelinedetail.common.tab.pullrequests', { defaultValue: 'Pull Requests' }) }</TabLink>,
        ];

        const pageHeader = isReady ? (
                <ContentPageHeader pageTabBase={baseUrl} pageTabLinks={pageTabLinks}>
                    <WeatherIcon score={pipeline.weatherScore} />
                    <h1>
                        {AppConfig.showOrg() && <span><Link to={orgUrl} query={location.query}>{organizationDisplayName}</Link>
                            <span>&nbsp;/&nbsp;</span></span>}
                            <Link to={activityUrl} query={location.query}>
                                <ExpandablePath path={fullDisplayName} hideFirst className="dark-theme" iconSize={20} />
                            </Link>
                    </h1>
                    <Extensions.Renderer
                        extensionPoint="jenkins.pipeline.detail.header.action"
                        store={this.context.store}
                        pipeline={pipeline}
                    />
                    {classicConfigLink(pipeline)}
                </ContentPageHeader>
            ) : (
                <ContentPageHeader pageTabBase={baseUrl} pageTabLinks={pageTabLinks}>
                    <h1>
                        <Link to={orgUrl}>{organizationDisplayName}</Link>
                        <span> / </span>
                    </h1>
                </ContentPageHeader>
            );

        return (
            <Page>
                { pageHeader }
                {isReady && React.cloneElement(this.props.children, { pipeline, setTitle, t: translate, locale: translate.lng })}
            </Page>
        );
    }
}

PipelinePage.propTypes = {
    children: PropTypes.any,
    pipeline: PropTypes.any,
    params: PropTypes.object,
    setTitle: PropTypes.func,
};


PipelinePage.contextTypes = {
    config: PropTypes.object.isRequired,
    location: PropTypes.object,
    store: PropTypes.object,
    pipelineService: PropTypes.object,
};

export default documentTitle(PipelinePage);

