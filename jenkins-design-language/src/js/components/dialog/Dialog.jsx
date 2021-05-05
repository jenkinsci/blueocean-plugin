// @flow
import React, { Component, PropTypes } from 'react';

import { ModalContainer } from './ModalContainer';

//--------------------------------------------------------------------------
//
//  Basic Dialog
//
//--------------------------------------------------------------------------

/**
 Basic, bare-bones Dialog component:

 - Show the user a styled dialog, with any child content to be set by owner.
 - Listen for and re-dispatch user dismiss requests.

 */
export class BasicDialog extends Component {
    props: {
        className?: string,
        onDismiss?: Function,
        ignoreEscapeKey?: boolean,
        children?: ReactChildren,
    };

    //--------------------------------------
    //  User Interaction
    //--------------------------------------

    modalScreenClicked = () => {
        const { onDismiss } = this.props;

        if (onDismiss) {
            onDismiss();
        }
    };

    keyPressed = (event: KeyboardEvent) => {
        // TODO: Move this into ModalContainer, and remove from Popover as well!
        const { onDismiss, ignoreEscapeKey } = this.props;

        if (!ignoreEscapeKey && onDismiss && event.keyCode === 27) {
            onDismiss();
        }
    };

    //--------------------------------------
    //  React Lifecycle
    //--------------------------------------

    render() {
        const { className, children } = this.props;
        const newClassName = (className ? className + ' ' : '') + ' Dialog';

        return (
            <ModalContainer onScreenClick={this.modalScreenClicked}>
                <div className={newClassName}>{children}</div>
            </ModalContainer>
        );
    }

    componentDidMount() {
        document.addEventListener('keyup', this.keyPressed, false);
        // TODO: Remove from here and Popover
    }

    componentWillUnmount() {
        document.removeEventListener('keyup', this.keyPressed, false);
        // TODO: Remove from here and Popover
    }
}

BasicDialog.propTypes = {
    onDismiss: PropTypes.func,
    ignoreEscapeKey: PropTypes.bool,
    children: PropTypes.node,
};

//--------------------------------------------------------------------------
//
//  Dialog Header / Title Bar
//
//--------------------------------------------------------------------------

/** Basic header for dialogs */
export class DialogHeader extends Component {
    props: {
        children?: ReactChildren,
    };

    render() {
        return (
            <div className="Dialog-header">
                <h3>{this.props.children}</h3>
            </div>
        );
    }
}

DialogHeader.propTypes = {
    children: PropTypes.node,
};

//--------------------------------------------------------------------------
//
//  Scrolling Content Pane
//
//--------------------------------------------------------------------------

/** Wraps the content of a dialog to provide a scrollbar if there's too much to fit */
export class DialogContent extends Component {
    props: {
        children?: ReactChildren,
    };

    render() {
        return (
            <div className="Dialog-content-scroll">
                <div className="Dialog-content-margin">{this.props.children}</div>
            </div>
        );
    }
}

DialogContent.propTypes = {
    children: PropTypes.node,
};

//--------------------------------------------------------------------------
//
//  Button bar
//
//--------------------------------------------------------------------------

/** A container for dialog action buttons */
export class DialogButtonBar extends Component {
    props: {
        children?: ReactChildren,
    };

    render() {
        const { children } = this.props;

        return <div className="Dialog-button-bar">{children}</div>;
    }
}

DialogButtonBar.propTypes = {
    children: PropTypes.node,
};

//--------------------------------------------------------------------------
//
//  Easy-to-use Dialog
//
//--------------------------------------------------------------------------

/** An easy-to-use Dialog component with a title and button bar, and scroll pane for children */
export class Dialog extends Component {
    userDismissed = () => {
        const { onDismiss } = this.props;
        if (onDismiss) {
            onDismiss(this);
        }
    };

    render() {
        const { className, title, buttons, children, ignoreEscapeKey } = this.props;

        const defaultButton = <button onClick={this.userDismissed}>Close</button>;
        const buttonArray = [].concat(buttons || defaultButton);

        // Doing this will avoid getting annoying messages from React about array keys
        const buttonBar = React.createElement(DialogButtonBar, {}, ...buttonArray);

        return (
            <BasicDialog className={className} ignoreEscapeKey={ignoreEscapeKey} onDismiss={this.userDismissed}>
                <DialogHeader>{title}</DialogHeader>
                <DialogContent>{children}</DialogContent>
                {buttonBar}
            </BasicDialog>
        );
    }
}

Dialog.propTypes = {
    className: PropTypes.string,
    title: PropTypes.node,
    buttons: PropTypes.node,
    children: PropTypes.node,
    onDismiss: PropTypes.func,
    ignoreEscapeKey: PropTypes.bool,
};
