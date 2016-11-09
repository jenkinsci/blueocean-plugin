import React, { Component, PropTypes } from 'react';
import { pipelineService } from '@jenkins-cd/blueocean-core-js';

class Dashboard extends Component {

    constructor(props) {
        super(props);
        this._context = {};
        this._context.pipelineService = pipelineService;
    }
    getChildContext() {
        this._context.params = this.props.params;
        this._context.location = this.props.location;
        return this._context;
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
    pipelineService: PropTypes.object,
};

export default Dashboard;
