import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class PipelineActivityTab extends React.Component {
    render() {
        return <TabLink to={`${this.props.baseLink}/activity`}>Activity</TabLink>;
    }
};

PipelineActivityTab.propTypes = {
    pipeline: React.PropTypes.any,
};
