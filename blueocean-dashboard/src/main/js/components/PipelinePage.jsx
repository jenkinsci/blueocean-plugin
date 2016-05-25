import React, { Component, PropTypes } from 'react';
import {
    Page,
    PageHeader,
    Title,
    PageTabs,
    TabLink,
    WeatherIcon,
    Favorite,
} from '@jenkins-cd/design-language';
import { urlPrefix } from '../config';

const { object } = PropTypes;

export default class PipelinePage extends Component {
    render() {
        const { pipeline } = this.context;

        if (!pipeline) {
            return null; // Loading...
        }

        return (
            <Page>
                <PageHeader>
                    <Title>
                        <WeatherIcon score={pipeline.weatherScore} size="large" />
                        <h1>{pipeline.organization} / {pipeline.name}</h1>
                        <Favorite darkTheme />
                    </Title>
                    <PageTabs base={`${urlPrefix}/${pipeline.name}`}>
                        <TabLink to="/activity">Activity</TabLink>
                        <TabLink to="/branches">Branches</TabLink>
                        <TabLink to="/pr">Pull Requests</TabLink>
                    </PageTabs>
                </PageHeader>
                {React.cloneElement(this.props.children, { pipeline })}
            </Page>
        );
    }
}

PipelinePage.propTypes = {
    children: object,
};

PipelinePage.contextTypes = {
    pipeline: object,
};
