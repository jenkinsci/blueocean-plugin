import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class PipelinePullRequestsTab extends React.Component {
    render() {
        return <TabLink to={`${this.props.baseLink}/pr`}>Pull Requests</TabLink>;
    }
}

PipelinePullRequestsTab.propTypes = {
    pipeline: React.PropTypes.any,
    baseLink: React.PropTypes.string,
};
