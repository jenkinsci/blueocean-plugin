import React, { Component, PropTypes } from 'react';
import { UrlConfig } from '@jenkins-cd/blueocean-core-js';
const { object, node } = PropTypes;

class Dashboard extends Component {

    getChildContext() {
        const {
            params,
            location,
        } = this.props;

        return {
            params,
            location,
        };
    }

    render() {
        return this.props.children; // Set by router
    }
}

Dashboard.propTypes = {
    params: object, // From react-router
    children: node, // From react-router
    location: object, // From react-router
};

Dashboard.childContextTypes = {
    params: object, // From react-router
    location: object, // From react-router
};

export default Dashboard;
