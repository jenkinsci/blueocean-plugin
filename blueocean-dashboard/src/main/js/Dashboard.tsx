import * as React from 'react';
import { pipelineService, activityService } from '@jenkins-cd/blueocean-core-js';
import navState from './DashboardNavState';

interface Props {
    params: object;
    children?: React.ReactNode;
    location: object;
}

interface DashboardContext {
    pipelineService?: object;
    activityService?: object;
    params?: object;
    location?: object;
}
class Dashboard extends React.Component<Props> {
    static childContextTypes = {
        params: React.PropTypes.object, // From react-router
        location: React.PropTypes.object, // From react-router
        pipelineService: React.PropTypes.object,
        activityService: React.PropTypes.object,
    };
    private _context: DashboardContext;

    constructor(props) {
        super(props);
        this._context = { pipelineService, activityService };
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
