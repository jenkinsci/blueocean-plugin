import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { StatusIndicator } from '@jenkins-cd/design-language';

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
            <td>{moment(latestRun.endTime).fromNow()}</td>
        </tr>);
    }
}

PullRequest.propTypes = {
    pr: PropTypes.object,
};
