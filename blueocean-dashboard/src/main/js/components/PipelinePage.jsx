import React, { Component, PropTypes } from 'react';
import {
    actions,
    pipeline as pipelineSelector,
    connect,
    createSelector,
} from '../redux';
import { Link } from 'react-router';
import Extensions from '@jenkins-cd/js-extensions';
import NotFound from './NotFound';
import {
    Page,
    PageHeader,
    Title,
    PageTabs,
    TabLink,
    WeatherIcon,
} from '@jenkins-cd/design-language';
import PageLoading from './PageLoading';
import { buildOrganizationUrl, buildPipelineUrl } from '../util/UrlUtils';

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
    
    _componentWillReceiveProps(nextProps) {
        if (this.props.params.organziation !== nextProps.params.organization
        || this.props.params.pipeline !== nextProps.params.pipeline) {
            nextProps.fetchPipeline(nextProps.params.organization, nextProps.params.pipeline);
        }
    }
    
    render() {
        const { pipeline } = this.props;
        const { organization, name, fullName } = pipeline || {};
        const orgUrl = buildOrganizationUrl(organization);
        const activityUrl = buildPipelineUrl(organization, fullName, 'activity');
        const isReady = pipeline && !pipeline.$pending;
        
        if (pipeline && pipeline.$failed) {
            return <NotFound />;
        }

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
                            <Link to={orgUrl}>{organization}</Link>
                            <span> / </span>
                            <Link to={activityUrl}>{name}</Link>
                        </h1>
                        <Extensions.Renderer
                          extensionPoint="jenkins.pipeline.detail.header.action"
                          store={this.context.store}
                          pipeline={this.context.pipeline}
                        />
                    </Title>
                    }
                    <PageTabs base={baseUrl}>
                        <TabLink to="/activity">Activity</TabLink>
                        <TabLink to="/branches">Branches</TabLink>
                        <TabLink to="/pr">Pull Requests</TabLink>
                    </PageTabs>
                </PageHeader>
                {isReady && React.cloneElement(this.props.children, { pipeline })}
            </Page>
        );
    }
}

PipelinePage.propTypes = {
    children: PropTypes.any,
    fetchPipeline: PropTypes.func.isRequired,
    pipeline: PropTypes.any,
    params: PropTypes.object,
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

export default connect(selectors, actions)(PipelinePage);
