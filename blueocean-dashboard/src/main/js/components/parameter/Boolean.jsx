import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { DebugRender } from './DebugRender';


export class Boolean extends Component {

    render() {
        return React.createElement(DebugRender, this.props);
    }
}

Boolean.propTypes = propTypes;
