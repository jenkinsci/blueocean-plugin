import React, {Component, PropTypes} from 'react';
import Table from './Table';
import Branches from './Branches'
import {components} from '@jenkins-cd/design-language';
const { WeatherIcon } = components;

export default class MultiBranch extends Component {
  render() {
    const {pipeline, back} = this.props;
    //early out
    if(!pipeline) { return null; }
    const {
        name,
        weatherScore
      } = pipeline;
    const headers = [
      'Health', 'Status', 'Branch', 'Last commit', 'Latest message', 'Completed'
    ];

    return (<div>
      <div>
        <div>
          <WeatherIcon score={weatherScore}/>&nbsp;CloudBees / {name}
        </div>
        <div>
          <button>Activity</button>
          Branches
          <button>Pull Requests</button>
        </div>
      </div>
      <Table
        className="multiBranch"
        headers={headers}>
        {pipeline.branchNames.map((branch, index) => <Branches key={index} branch={branch} pipeline={pipeline}/>)}
        <tr>
          <td colSpan={headers.length}>
            <button onClick={back}>Dashboard</button>
          </td>
        </tr>
      </Table>
    </div>)
  }
}

MultiBranch.propTypes = {
  pipeline: PropTypes.object.isRequired,
  back: PropTypes.func.isRequired
};



