import React, { Component, PropTypes } from 'react';
import AjaxHoc from '../AjaxHoc';
import moment from 'moment';
import _ from 'lodash';
import { components } from '@jenkins-cd/design-language';
const { WeatherIcon } = components;

/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/branches/quicker/runs/
 */
// FIXME: from here I need to get the related data for each branch
export class Branche extends Component {
    render() {
      const { pipeline, branch, data } = this.props;
      let
        commit,
        latestRun = {}, // the last run
        latestCommit = {}, // the last commit we have found (may differ from latest run
        msgCommit = '', // the message from the commit
        idCommit = '' // the id of the commit (we later shorten it to 8 char
      ;
    // early out
      if (!pipeline && !data && !data.toJS) { return null; }
    // process the run data
      if (data && data.toJS) {
        latestRun = _.maxBy(data.toJS(), 'id');
        latestCommit = _.maxBy(data.toJS(), run => {
          if (run.changeSet[0]) {
            return run.id;
        }
      });
        if (latestRun.changeSet[0]) {
          commit = latestRun.changeSet[0];
      } else if (latestCommit) {
          commit = latestCommit.changeSet[0];
      }
        if (commit) {
          let { comment, commitId } = commit;
          msgCommit = comment;
          idCommit = commitId.substring(0, 8);
      }
    }

      return <tr key={branch}>
      <td><WeatherIcon score={pipeline.weatherScore} /></td>
      <td>{latestRun.result}</td>
      <td>{branch}</td>
      <td>{idCommit}</td>
      <td>{msgCommit}</td>
      <td>{moment(latestRun.endTime).fromNow()}</td>
    </tr>;
  }
}

Branche.propTypes = {
    pipeline: PropTypes.object.isRequired,
    branch: PropTypes.string.isRequired
};

const baseUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines/';

export default AjaxHoc(Branche, props => ({
    url: `${baseUrl}${props.pipeline.name}/branches/${props.branch}/runs`
}));
