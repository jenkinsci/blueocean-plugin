import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class FreestyleDeploymentsTab extends React.Component {
    render() {
        return (
            <TabLink to={`${this.props.baseLink}/deployments`}>Deployments</TabLink>
        );
    }
};
