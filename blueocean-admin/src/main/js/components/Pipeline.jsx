import React, {Component, PropTypes} from 'react';
import {components} from 'jenkins-design-language';
const { WeatherIcon, Table } = components;

/*
 {
 'displayName': 'beers',
 'name': 'beers',
 'organization': 'jenkins',
 'weatherScore': 0,
 'branchNames': ['master'],
 'numberOfFailingBranches': 1,
 'numberOfFailingPullRequests': 0,
 'numberOfSuccessfulBranches': 0,
 'numberOfSuccessfulPullRequests': 0,
 'totalNumberOfBranches': 1,
 'totalNumberOfPullRequests': 0
 }
 {}
 */

export default class Pipeline extends Component {

  constructor(props) {
    super(props);
    this.state = {clicked: false};
  }

  render() {
    const
      { pipeline, simple = false } = this.props,
      {
        name,
        weatherScore,
        numberOfSuccessfulBranches,
        numberOfFailingBranches,
        numberOfSuccessfulPullRequests,
        numberOfFailingPullRequests
      } = pipeline;

    let
      multiBranch,
      multiPr;

    if(!simple) {
      multiBranch = (<td>
        {numberOfSuccessfulBranches} passing
        | {numberOfFailingBranches} failing
      </td>);
      multiPr = (<td>{numberOfSuccessfulPullRequests} passing
      | {numberOfFailingPullRequests} failing
      </td>);
    } else {
      multiBranch = multiPr = (<td></td>)
    }

    return (<tr key={name}>
      <td>
        {name}
      </td>
      <td><WeatherIcon score={weatherScore}/></td>
      {multiBranch}
      {multiPr}
      <td><i className='material-icons'>&#xE83A;</i></td>
    </tr>);
  }
}

Pipeline.propTypes = {
  pipeline: PropTypes.object.isRequired,
  simple: PropTypes.bool
};
