import React, {Component} from 'react';
import Immutable from 'immutable';
import  Dashboard from './components/Dashboard';

export default class OrganisationPipelines extends Component {
  constructor() {
    super();
    this.state = {pipelines: null};
  }

  componentDidMount() {
    fetchPipelineData((data) => {
      this.setState({
        pipelines: Immutable.fromJS(data)
      });
    });
  }

  render() {
    return this.state.pipelines ? <Dashboard pipelines={this.state.pipelines}/> : null;
  }
}

/** FIXME: Ghetto ajax loading of pipeline data for an org @store*/
function fetchPipelineData(onLoad) {
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange =  () => {
    if (xmlhttp.readyState == XMLHttpRequest.DONE) {
      if (xmlhttp.status == 200) {
        var pipes = JSON.parse(xmlhttp.responseText);
        onLoad(pipes);
      } else {
        console.log('something else other than 200 was returned')
      }
    }
  };
  xmlhttp.open("GET", "/jenkins/blue/rest/organizations/jenkins/pipelines/", true);
  xmlhttp.send();
}
