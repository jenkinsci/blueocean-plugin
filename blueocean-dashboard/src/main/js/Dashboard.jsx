import React, { Component, PropTypes } from 'react';
import { pipelineService, activityService } from '@jenkins-cd/blueocean-core-js';
import navState from './DashboardNavState';


class Dashboard extends Component {

    constructor(props) {
        super(props);
        this._context = {};
        this._context.pipelineService = pipelineService;
        this._context.activityService = activityService;
    }

    getChildContext() {
        this._context.params = this.props.params;
        this._context.location = this.props.location;
        return this._context;
    }

    componentWillMount() {
        navState.setActive();
    }

    componentWillUnmount() {
        navState.setInactive();
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
    activityService: PropTypes.object,
};

export default Dashboard;
