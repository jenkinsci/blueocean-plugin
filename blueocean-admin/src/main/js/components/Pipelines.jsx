import React, {Component, PropTypes} from 'react';
import Pipeline from './Pipeline';
import {components} from 'jenkins-design-language';
const { Table } = components;

export default class Pipelines extends Component {

  render() {
    const {pipelines} = this.props;
    const multiBranch = pipelines.filter(pipeline => {
      return !!pipeline.branchNames;
    });
    const noMultiBranch = pipelines.filter(pipeline => {
      return !pipeline.branchNames;
    });
    // Early out
    if (!pipelines) {
      return null;
    }
    return (<div>
      {multiBranch && (<div>multiBranch:
        <Table className="multiBranch">
          <thead>
          <tr>
            <th>Name/<span style={{color:'green'}}>BranchName(s)</span></th>
            <th>Status</th>
            <th>Branches</th>
            <th>Pull Requests</th>
            <th></th>
          </tr>
          </thead>
          <tbody>
          { multiBranch.map(
            (pipeline, index) => <Pipeline key={index} pipeline={pipeline}/>
          )}
          </tbody>
        </Table>
      </div>)}
      {noMultiBranch && (<div>simplePipe:
        <Table className="simplePipe">
          <thead>
          <tr>
            <th>Name</th>
            <th>Status</th>
            <th></th>
          </tr>
          </thead>
          <tbody>
          { noMultiBranch.map((pipeline, index) => <Pipeline
            key={index}
            simple={true}
            pipeline={pipeline}/>)}
          </tbody>
        </Table>
      </div>)}
    </div>);
  }
}

Pipelines.propTypes = {
  pipelines: PropTypes.array.isRequired
};