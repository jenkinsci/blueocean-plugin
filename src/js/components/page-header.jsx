// @flow

import React, { Component, PropTypes } from 'react';
import {Link} from 'react-router';

export class PageHeader extends Component {
    render() {
        return <header className="sub-header">{this.props.children}</header>;
    }
}

PageHeader.propTypes = {
    children: PropTypes.node
};

export class Title extends Component {
    render() {
        // If we've been given a plain string, wrap it as <h1>
        var contents = this.props.children;
        if (typeof contents === "string") {
            contents = <h1>contents</h1>;
        }

        return <nav className="page-title">{contents}</nav>;
    }
}

Title.propTypes = {
    children: PropTypes.node,
};

export class PageTabs extends Component {
    render() {
        const base = this.props.base;
        return (
            <nav className="page-tabs">
                {React.Children.map(this.props.children, child => React.cloneElement(child, {base}))}
            </nav>
        );
    }
}

PageTabs.propTypes = {
    children: PropTypes.node,
    base: PropTypes.string
};

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