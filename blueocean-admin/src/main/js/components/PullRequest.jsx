import React, { Component, PropTypes } from 'react';
import moment from 'moment';

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
      return (<tr key={latestRun.id}>
        <td>{latestRun.result}</td>
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
