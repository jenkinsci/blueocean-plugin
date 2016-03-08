import React, {Component, PropTypes} from 'react';
import Pipeline, { PipelineRecord } from './Pipeline';
import Immutable from 'immutable';
import Table from './Table';

export default class Pipelines extends Component {

  render() {
    const {pipelines} = this.props;
    // Early out
    if (!pipelines) {
      return null;
    }
    const multiBranch = pipelines.filter(pipeline => {
      return !! new PipelineRecord(pipeline).branchNames;
    });
    const noMultiBranch = pipelines.filter(pipeline => {
      return !new PipelineRecord(pipeline).branchNames;
    });
    return (
      <Table
        className="multiBranch"
        headers={['Name', 'Status', 'Branches', 'Pull Requests', '']}>
        { multiBranch.map(
          (pipeline, index) => <Pipeline
            key={index}
            pipeline={new PipelineRecord(pipeline)}/>
        )}
        { noMultiBranch.map(
          (pipeline, index) => <Pipeline
            key={index}
            simple={true}
            pipeline={new PipelineRecord(pipeline)}/>)}
      </Table>);
  }
}

Pipelines.propTypes = {
  pipelines: PropTypes.object.isRequired
};
