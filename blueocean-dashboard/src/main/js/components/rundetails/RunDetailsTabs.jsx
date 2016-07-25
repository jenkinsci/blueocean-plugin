import React from 'react';
import { TabLink } from '@jenkins-cd/design-language';

export default class RunDetailsTabs extends React.Component {
    render() {
        return (
            <span>
                <TabLink to={`${this.props.baseLink}/pipeline`}>Pipeline</TabLink>
                <TabLink to={`${this.props.baseLink}/changes`}>Changes</TabLink>
                <TabLink to={`${this.props.baseLink}/tests`}>Tests</TabLink>
            </span>
        );
    }
}

RunDetailsTabs.propTypes = {
    pipeline: React.PropTypes.any,
    baseLink: React.PropTypes.string,
};
