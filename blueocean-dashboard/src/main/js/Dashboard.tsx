import React, { Component, PropTypes, ReactNode } from 'react';
import { pipelineService, activityService } from '@jenkins-cd/blueocean-core-js';
import navState from './DashboardNavState';

interface DashboardProps {
    params: object,
    chilren: ReactNode,
    location: object
}

interface DashBoardContext {
    pipelineService?: object,
    activityService?: object,
    params?: object,
    location?: object
}
class Dashboard extends Component<DashboardProps, any> {
    private _context: DashBoardContext

    constructor(props) {
        super(props);
        this._context = { pipelineService, activityService }
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
        return this.props.children as JSX.Element; // Set by router
    }
}

export default Dashboard;
