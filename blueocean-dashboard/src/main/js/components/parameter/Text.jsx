import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { DebugRender } from './DebugRender';

export class Text extends Component {
    render() {
        return React.createElement(DebugRender, this.props);
    }
}

Text.propTypes = propTypes;
