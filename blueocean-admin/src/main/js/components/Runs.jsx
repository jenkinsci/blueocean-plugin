import React, {Component, PropTypes} from 'react';
import AjaxHoc from '../AjaxHoc';
import Immutable from 'immutable';
import moment from 'moment';
import _ from 'lodash';
require('moment-duration-format');

export const ActivityRecord = Immutable.Record({
  changeSet: [{
    author: {
      email: null,
      fullName: null,
      id: null
    },
    affectedPaths: null,
    commitId: null,
    comment: null,
    date: null,
    id: null,
    msg:null,
    paths: null,
    timestamp: null
  }],
  durationInMillis: null,
  enQueueTime: null,
  endTime: null,
  id: null,
  organization: null,
  pipeline: null,
  result: null,
  runSummary: null,
  startTime: null,
  state: null,
  type: null,
  commitId: null
});

export const ChangeSetRecord = Immutable.Record({
  author: {
    email: null,
    fullName: null,
    id: null
  },
  affectedPaths: null,
  commitId: null,
  comment: null,
  date: null,
  id: null,
  msg:null,
  paths: null,
  timestamp: null
})
/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/runs
 */
export default class Runs extends Component {
  render() {
    const {data, changeset} = this.props;
    //early out
    if (!data && data.toJS) { return null; }
    let
      duration = moment.duration(Number(data.durationInMillis), 'milliseconds').format('hh:mm:ss');

    const
      durationArray = duration.split(':');

    if (durationArray.length === 1){
      duration = `00:${duration}`;
    }

    return <tr key={data.id}>
      <td>{data.result}</td>
      <td>{data.id}</td>
      <td>{changeset && changeset.commitId && changeset.commitId.substring(0, 8)}</td>
      <td>{data.pipeline}</td>
      <td>{changeset && changeset.comment}</td>
      <td>
        {duration} minutes</td>
      <td>{moment(data.endTime).fromNow()}</td>
    </tr>;
  }
}

Runs.propTypes = {
  data: PropTypes.object.isRequired,
  changeset: PropTypes.object.isRequired,
};

