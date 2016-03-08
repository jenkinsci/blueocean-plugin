import React, {Component, PropTypes} from 'react';
import Table from './Table';
/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/branches/quicker/runs/
 */

export default class MultiBranch extends Component {
  render() {
    const {pipeline} = this.props;
    //early out
    if(!pipeline) { return null; }
    const headers = [
      'Health', 'Status', 'Branch', 'Last commit', 'Latest message', 'Completed'
    ];
//FIXME: from here I need to get the related data for each branch
    return (<Table
        className="multiBranch"
        headers={headers}>
      {pipeline.branchNames.map((branch, index) => <tr key={branch}>
        <td></td>
        <td></td>
        <td>{branch}</td>
        <td></td>
        <td></td>
        <td></td>
      </tr>)}
      <tr>
        <td colSpan={headers.length}>
          The roof is on fire @{pipeline.displayName}
        </td>
      </tr>
    </Table>)
  }
}

MultiBranch.propTypes = {
  pipeline: PropTypes.object.isRequired
};
