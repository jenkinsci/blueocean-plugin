/**
 * Created by cmeyers on 7/27/16.
 */
import React, { Component, PropTypes } from 'react';

/**
 * Stops propagation of click events inside this container.
 * Useful for areas in UI where children should always handle the event, no matter what parent listeners are bound.
 *
 * This is a workaround for the following scenario:
 * 1. Parent DOM element has a click listener,
 * 2. Child DOM element added via an extension point calls event.stopPropagation() in its own click listener.
 *
 * This fails to work, even when calling stopProp and stopImmediateProp on the native event,
 * probably beacuse there are two React trees each with their own document listener.
 *
 * see: http://stackoverflow.com/questions/24415631/reactjs-syntheticevent-stoppropagation-only-works-with-react-events
 */
export class StopPropagation extends Component {

    _suppressEvent(event) {
        event.stopPropagation();
    }

    render() {
        return (
            <span
              className={this.props.className}
              onClick={(event) => this._suppressEvent(event)}
            >
                {this.props.children}
            </span>
        );
    }
}

export const stopProp = (event) => {
    event.stopPropagation();
};

StopPropagation.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
};
