import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class RunDetailsArtifactsTab extends React.Component {
    render() {
        return (
            <TabLink to={`${this.props.baseLink}/artifacts`}>Artifacts</TabLink>
        );
    }
};

RunDetailsArtifactsTab.propTypes = {
    pipeline: React.PropTypes.any,
};
