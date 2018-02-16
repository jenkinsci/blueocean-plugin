// @flow

import React, { Component, PropTypes } from 'react';

export class Page extends Component {
    render() {
        return <div id="outer">{this.props.children}</div>;
    }
}

Page.propTypes = {
    children: PropTypes.node,
};
