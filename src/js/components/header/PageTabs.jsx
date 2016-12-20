// @flow

import React, { Component, PropTypes } from 'react';

export class PageTabs extends Component {
    render() {
        const { base, children } = this.props;
        return (
            <nav className="Header-pageTabs">
                {React.Children.map(children, child => React.cloneElement(child, {base}))}
            </nav>
        );
    }
}

PageTabs.propTypes = {
    children: PropTypes.node,
    base: PropTypes.string
};
