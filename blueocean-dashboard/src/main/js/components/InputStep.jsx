import React, { Component, PropTypes } from 'react';

export default class InputStep extends Component {

    render() {
        const { node } = this.props;
        // Early out
        if (!node) {
            return null;
        }

        return (<div className="inputStep">
            <span>FIXME: generate Form from input</span>
      </div>);
    }
}

const { shape } = PropTypes;
Node.propTypes = {
    node: shape.isRequired,
};
