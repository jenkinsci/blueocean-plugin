import React, {Component} from 'react';
import Immutable from 'immutable';
import  Dashboard from './components/Dashboard';
import AjaxHoc from './AjaxHoc'

class OrganisationPipelines extends Component {
  render() {
    return this.props.data ? <Dashboard pipelines={this.props.data}/> : null;
  }
}
const baseUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines/';

export default AjaxHoc(OrganisationPipelines ,props =>({
  url: baseUrl
}));
