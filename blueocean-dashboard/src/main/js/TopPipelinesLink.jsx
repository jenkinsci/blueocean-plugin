import React, { Component } from 'react';
import { Link } from 'react-router';
import { observer } from 'mobx-react';

import dashboardNavState from './DashboardNavState';


@observer
export default class TopPipelinesLink extends Component {
    render() {
        const className = dashboardNavState.isActive ? 'selected' : '';
        return (
            <Link className={className} to="/pipelines">Pipelines</Link>
        );
    }
}
