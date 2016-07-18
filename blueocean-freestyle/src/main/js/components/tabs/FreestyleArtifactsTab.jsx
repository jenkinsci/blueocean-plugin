import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class FreestyleArtifactsTab extends React.Component {
    render() {
        return (
            <TabLink to={`${this.props.baseLink}/freestyle-artifacts`}>Freestyle Artifacts</TabLink>
        );
    }
};
