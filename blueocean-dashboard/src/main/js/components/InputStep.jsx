import React, { Component, PropTypes } from 'react';
import { supportedInputTypesMapping } from './parameter/index';

export default class InputStep extends Component {

    render() {
        const { node } = this.props;
        // Early out
        if (!node) {
            return null;
        }
        const { input: { message, parameters } } = node;

        return (<div className="inputStep">
            <h1>{message}</h1>
            {
                parameters.map((parameter, index) => {
                    const { type } = parameter;
                    const returnValue = supportedInputTypesMapping[type];
                    if (returnValue) {
                        return React.createElement(returnValue, { ...parameter, key: index });
                    }
                    return <div>No component found for type {type}.</div>;
                })
            }
        </div>);
    }
}

const { shape } = PropTypes;

InputStep.propTypes = {
    node: shape().isRequired,
};
