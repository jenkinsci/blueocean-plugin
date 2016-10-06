import * as sse from '@jenkins-cd/sse-gateway';
import React, { Component, PropTypes } from 'react';
import { UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { PipelinesService } from './model/PipelinesService';


// Connect to the SSE Gateway and allocate a
// dispatcher for blueocean.
// TODO: We might want to move this code to a local SSE util module.
sse.connect({
    clientId: 'jenkins_blueocean',
    onConnect: undefined,
    jenkinsUrl: `${UrlConfig.getJenkinsRootURL()}/`, // FIXME sse should not require this to end with a /
});

class Dashboard extends Component {

    getChildContext() {
        const {
            params,
            location,
        } = this.props;

        return {
            params,
            location,
            pipelinesService: new PipelinesService(),
        };
    }

    render() {
        return this.props.children; // Set by router
    }
}

Dashboard.propTypes = {
    params: PropTypes.object, // From react-router
    children: PropTypes.node, // From react-router
    location: PropTypes.object, // From react-router
};

Dashboard.childContextTypes = {
    params: PropTypes.object, // From react-router
    location: PropTypes.object, // From react-router
    pipelinesService: PropTypes.object,
};

export default Dashboard;
