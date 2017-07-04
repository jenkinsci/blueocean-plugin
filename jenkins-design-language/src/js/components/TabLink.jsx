// @flow

import React, { Component, PropTypes } from 'react';
import {Link} from 'react-router';

export class TabLink extends Component {
    render() {
        const { router = {}, location = {} } = this.context;
        const { base = '', to: toOrg = '' } = this.props;
        const to = (toOrg).substring(1);
        const routeUrl = base + '/' + to;
        const linkClassName = router.isActive(routeUrl) ? "selected " + to : to;
        return (<Link
          to={routeUrl}
          query={location.query}
          className={linkClassName}
        >
            {this.props.children}
        </Link>);
    }
}

TabLink.propTypes = {
    children: PropTypes.node,
    base: PropTypes.string,
    to: PropTypes.oneOfType([
        PropTypes.string.isRequired,
        PropTypes.shape.isRequired,
    ])
};

TabLink.contextTypes = {
    router: PropTypes.object,
    location: PropTypes.object.isRequired, // From react-router
};
