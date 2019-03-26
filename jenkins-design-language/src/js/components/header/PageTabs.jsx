// @flow

import React, { Component } from 'react';
import PropTypes from 'prop-types';

export class PageTabs extends Component {
    render() {
        const { base, children } = this.props;
        return <nav className="Header-pageTabs">{React.Children.map(children, child => child && React.cloneElement(child, { base }))}</nav>;
    }
}

PageTabs.propTypes = {
    children: PropTypes.node,
    base: PropTypes.string,
};
