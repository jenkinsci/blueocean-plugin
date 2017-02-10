import React, { Component, PropTypes } from 'react';
import { pipelineService, activityService } from '@jenkins-cd/blueocean-core-js';
import {
    actions,
    allPipelines as allPipelinesSelector,
    organizationPipelines as organizationPipelinesSelector,
    connect,
    createSelector,
} from './redux';
import loadingIndicator from './LoadingIndicator';

class Dashboard extends Component {

    constructor(props) {
        super(props);
        this._context = {};
        this._context.pipelineService = pipelineService;
        this._context.activityService = activityService;
    }
    componentDidMount() {
        loadingIndicator.setDarkBackground();
    }
    componentWillUnmount() {
        loadingIndicator.setLightBackground();
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
    activityService: PropTypes.object,
};

const selectors = createSelector([allPipelinesSelector, organizationPipelinesSelector],
    (allPipelines, organizationPipelines) => ({ allPipelines, organizationPipelines }));

export default connect(selectors, actions)(Dashboard);
