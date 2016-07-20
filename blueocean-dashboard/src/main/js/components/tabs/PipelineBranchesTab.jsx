import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class PipelineBranchesTab extends React.Component {
    render() {
        return <TabLink to={`${this.props.baseLink}/branches`}>Branches</TabLink>;
    }
};
