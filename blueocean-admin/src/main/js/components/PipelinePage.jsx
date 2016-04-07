import React, { Component, PropTypes } from 'react';
import { Page, PageHeader, Title, PageTabs, TabLink, WeatherIcon  } from '@jenkins-cd/design-language';
import { urlPrefix } from '../config';

export default class PipelinePage extends Component {
    render() {
        const { pipeline } = this.context;

        if (!pipeline) {
            return null; // Loading...
        }

        return (
            <Page>
                <PageHeader>
                    <Title><WeatherIcon score={pipeline.weatherScore}/> <h1>CloudBees / {pipeline.name}</h1></Title>
                    <PageTabs base={urlPrefix + "/" + pipeline.name}>
                        <TabLink to="/activity">Activity</TabLink>
                        <TabLink to="/branches">Branches</TabLink>
                        <TabLink to="/pr">Pull Requests</TabLink>
                    </PageTabs>
                </PageHeader>
                {React.cloneElement(this.props.children, {pipeline})}
            </Page>
        );
    }
}

PipelinePage.contextTypes = {
    pipeline: PropTypes.object,
};