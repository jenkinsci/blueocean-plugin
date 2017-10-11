import React, { Component } from 'react';
import { propTypes } from '../commonProptypes';

export class DebugRender extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name, type } = this.props;
        return (
            <ul>
                <li>name: {name}</li>
                <li>value: {value}</li>
                <li>description: {description}</li>
                <li>name: {name}</li>
                <li>type: {type}</li>
            </ul>
        );
    }
}

DebugRender.propTypes = propTypes;
