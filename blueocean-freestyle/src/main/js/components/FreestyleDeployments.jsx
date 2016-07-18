import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';
export default class FreestyleDeployments extends React.Component {
    render() {
        return <div>Some deployments</div>;
    }
};

FreestyleDeployments.contextTypes = {
    pipeline: React.PropTypes.any,
};
