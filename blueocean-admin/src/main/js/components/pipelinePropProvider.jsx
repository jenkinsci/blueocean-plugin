import React, { Component, PropTypes } from 'react';

/**
 * Wraps a component, for the purpose of providing a "selected pipeline" from the react context (as a prop on wrapped
 * component). This is a transformer, not a constructor.
 *
 * @param WrappedComponent a react component class/constructor
 * @return {Wrapper}
 */
export default function pipelinePropProvider(WrappedComponent) {

    class Wrapper extends Component {
        render() {
            const { pipeline } = this.context;
            return <WrappedComponent {...this.props} pipeline={pipeline} />;
        }
    }

    Wrapper.contextTypes = {
        pipeline: PropTypes.object
    };

    return Wrapper;
}
