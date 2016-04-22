import React, { Component, PropTypes } from 'react';
import { ReadableDate, StatusIndicator } from '@jenkins-cd/design-language';

const { object } = PropTypes;

export default class PullRequest extends Component {
    render() {
        const { pr } = this.props;
        if (!pr) {
            return null;
        }
        const { latestRun, pullRequest } = pr;
        if (!latestRun || !pullRequest) {
            return null;
        }
        const result = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
        return (<tr key={latestRun.id}>
            <td><StatusIndicator result={result} /></td>
            <td>{latestRun.id}</td>
            <td>{pullRequest.title || '-'}</td>
            <td>{pullRequest.author || '-'}</td>
            <td><ReadableDate date={latestRun.endTime} /></td>
        </tr>);
    }
}

PullRequest.propTypes = {
    pr: object,
};
