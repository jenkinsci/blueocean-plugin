// @flow

import React, { Component } from 'react';
import PropTypes from 'prop-types';

export class Page extends Component {
    render() {
        return <div id="outer">{this.props.children}</div>;
    }
}

Page.propTypes = {
    children: PropTypes.node,
};
