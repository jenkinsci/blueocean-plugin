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
        branchNames,
        weatherScore,
        numberOfSuccessfulBranches,
        numberOfFailingBranches,
        numberOfSuccessfulPullRequests,
        numberOfFailingPullRequests
      } = pipeline;

    let
      multiBranch,
      multiPr,
      displayName = name;

    if(!simple) {
      multiBranch = (<td>
        {numberOfSuccessfulBranches} passing
        | {numberOfFailingBranches} failing
      </td>);
      multiPr = (<td>{numberOfSuccessfulPullRequests} passing
      | {numberOfFailingPullRequests} failing
      </td>);
      //FIXME: when we have a solution how to do it better @style
      displayName = (<a
        style={{
          color: this.state.clicked ? 'green' : 'black',
          cursor: 'pointer'
        }}
        onClick={() => this.setState({
          clicked: !this.state.clicked
        })}>
        {this.state.clicked ? <span>
            {branchNames.map( branchName => <span key={branchName}>{branchName}&nbsp;</span>)}
            <span style={{ color: 'black' }}>@{name}</span>
          </span>
          : name}
      </a>);

    }

    return (<tr key={name}>
      <td>
        {displayName}
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
