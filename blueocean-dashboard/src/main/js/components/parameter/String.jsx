import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { DebugRender } from './DebugRender';

export class String extends Component {

    render() {
        return React.createElement(DebugRender, this.props);
    }
}

String.propTypes = propTypes;
