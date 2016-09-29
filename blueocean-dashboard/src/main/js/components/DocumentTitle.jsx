import React, { Component } from 'react';

/**
 * Will inject setTitle as properties into the component which is using this.
 *
 *  Sample usage:
 * @example export default documentTitle(Pipelines);
 * // then in your code in the place that fits best
 * this.props.setTitle('Dashboard Jenkins – Dashboard'):  *
 * // Do not forget to declare the prop
 * Pipelines.propTypes = {
 *   setTitle: func,
 * } *
 * @param ComposedComponent
 */
export const documentTitle = ComposedComponent => class extends Component {

    /**
     * Set the title of the document
     * @param title {String}
     */
    setTitle(title) {
        if (document) {
            document.title = title;
        }
    }
    render() {
        // create a composedComponent and inject the functions we want to expose
        return (<ComposedComponent
          {...this.props}
          {...this.state}
          setTitle={this.setTitle}
        />);
    }
};
