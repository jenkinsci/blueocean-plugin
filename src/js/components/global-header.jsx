// @flow

import React, { Component, PropTypes } from 'react';

export class GlobalHeader extends Component {
    render() {
        return <header className="global-header">{this.props.children}</header>;
    }
}

GlobalHeader.propTypes = {
    children: PropTypes.node,
};

export class GlobalNav extends Component {
    render() {
        return <nav>{this.props.children}</nav>;
    }
}

GlobalNav.propTypes = {
    children: PropTypes.node,
};
