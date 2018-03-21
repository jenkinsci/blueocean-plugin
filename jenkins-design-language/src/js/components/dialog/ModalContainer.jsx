// @flow

import React, { Component, PropTypes } from 'react';

/**
 Wraps children in a .ModalContainer, shows a dark screen over page content, and listens for user dismiss events.

 This is where we'd want to manage show/hide animation if we add them at some point.

 FIXME: Isolate the tab-focus loop to children
 FIXME: Block user scroll input at .ModalContainer on capture
 */
export class ModalContainer extends Component {
    screenClicked = () => {
        const { onScreenClick } = this.props;

        if (onScreenClick) {
            onScreenClick(this);
        }
    };

    render() {
        return (
            <div className="ModalContainer">
                <div className="ModalContainer-modalScreen" onClick={this.screenClicked} />
                {this.props.children}
            </div>
        );
    }
}

ModalContainer.propTypes = {
    onScreenClick: PropTypes.func,
    children: PropTypes.element.isRequired,
};

export default ModalContainer;
