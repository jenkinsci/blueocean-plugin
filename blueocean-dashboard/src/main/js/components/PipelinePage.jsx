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
    ExpandablePath,
    Page,
    PageHeader,
    Title,
    PageTabs,
    TabLink,
    WeatherIcon,
} from '@jenkins-cd/design-language';
import PageLoading from './PageLoading';
import { buildOrganizationUrl, buildPipelineUrl } from '../util/UrlUtils';
import { documentTitle } from './DocumentTitle';

/**
 * returns true if the pipeline is defined and has branchNames
 */
export function pipelineBranchesUnsupported(pipeline) {
    if ((pipeline && !pipeline.branchNames) ||
        (pipeline && !pipeline.branchNames.length)) {
        return true;
    }
    return false;
}

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
                            <Link to={orgUrl}>{organization}</Link>
                            <span>&nbsp;/&nbsp;</span>
                            <Link to={activityUrl}>
                                <ExpandablePath path={fullDisplayName} iconSize={20} hideFirst />
                            </Link>
                        </h1>
                        <Extensions.Renderer
                          extensionPoint="jenkins.pipeline.detail.header.action"
                          store={this.context.store}
                          pipeline={this.props.pipeline}
                        />
                    </Title>
                    }
                    <PageTabs base={baseUrl}>
                        <TabLink to="/activity">Activity</TabLink>
                        <TabLink to="/branches">Branches</TabLink>
                        <TabLink to="/pr">Pull Requests</TabLink>
                    </PageTabs>
                </PageHeader>
                {isReady && React.cloneElement(this.props.children, { pipeline, setTitle })}
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

export default connect(selectors, actions)(documentTitle(PipelinePage));
