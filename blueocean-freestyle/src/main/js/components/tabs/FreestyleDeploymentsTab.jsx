import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class FreestyleDeploymentsTab extends React.Component {
    render() {
        return (
            <span>
                <TabLink to={`${this.props.baseLink}/deployments`}>Deployments</TabLink>
                <TabLink to={`/deployments/${this.props.pipeline._links.self.href}`}>Deployments 2</TabLink>
                <TabLink to={`/deployments`}>/deployments</TabLink>
            </span>
        );
    }
};
