import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { DebugRender } from './DebugRender';

export class Password extends Component {

    render() {
        return React.createElement(DebugRender, this.props);
    }
}

Password.propTypes = propTypes;
