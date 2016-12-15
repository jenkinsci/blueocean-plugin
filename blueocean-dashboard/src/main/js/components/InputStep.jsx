import React, { Component, PropTypes } from 'react';

export default class InputStep extends Component {

    render() {
        const { node } = this.props;
        // Early out
        if (!node) {
            return null;
        }
        const { input } = node;
	            console.log(node, input)

        return (<div className="inputStep">
            <span>FIXME: generate Form from input</span>
            <h1>{node.displayName}</h1>
      </div>);
    }
}

const { shape } = PropTypes;
Node.propTypes = {
    node: shape.isRequired,
};
