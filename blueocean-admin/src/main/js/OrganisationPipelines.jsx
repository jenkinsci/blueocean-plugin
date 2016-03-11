import React, { Component } from 'react';
import Dashboard from './components/Dashboard';
import AjaxHoc from './AjaxHoc';

class OrganisationPipelines extends Component {
    render() {
        // eslint-disable-next-line
        return this.props.data ? <Dashboard pipelines={this.props.data} /> : null;
    }
}
const baseUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines/';

// eslint-disable-next-line
export default AjaxHoc(OrganisationPipelines, props => ({
    url: baseUrl,
}));
