/**
 * Created by cmeyers on 8/17/16.
 */
import React, { Component, PropTypes } from 'react';
import TransitionGroup from 'react-addons-css-transition-group';

import { Toast } from './Toast';

/**
 * Toaster is a container for Toast instances displayed based on supplied 'toasts' prop.
 *
 * Supported props:
 * toasts - array of toast objects
 * {
 *     id: unique identifier
 *     text: string, message text to display
 *     action: string, text for action link
 *     onActionClick: function, callback to invoke when action link is clicked
 *     onDismiss: function, callback to invoke when toast is dismissed (immediately, or after timeout)
 *     dismissDelay: number, duration in millis after which to auto-dismiss this Toast
 * }
 * dismissDelay - number, default duration in millis after which to hide a Toast
 */
export class Toaster extends Component {

    constructor() {
        super();

        this.activeToasts = {};
    }

    componentWillMount() {
        this._initialize(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._initialize(nextProps);
    }

    _initialize(props) {
        if(!props.toasts) {
            return;
        }

        for (const toast of props.toasts) {
            const dismissDelay = toast.dismissDelay || props.dismissDelay || 5000;

            // if we aren't already tracking the toast, add an auto-dismiss handler and store the timeoutId
            if (!this.activeToasts[toast.id]) {
                const timeoutId = setTimeout(() => this._onDismiss(toast), dismissDelay);
                this.activeToasts[toast.id] = timeoutId;
            }
        }
    }

    _onActionClick(toast) {
        this._cleanup(toast);

        if (toast.onActionClick) {
            toast.onActionClick();
        }

        if (this.props.onActionClick) {
            this.props.onActionClick(toast);
        }
    }

    _onDismiss(toast) {
        this._cleanup(toast);

        if (toast.onDismiss) {
            toast.onDismiss();
        }

        if (this.props.onDismiss) {
            this.props.onDismiss(toast);
        }
    }

    _cleanup(toast) {
        const timeoutId = this.activeToasts[toast.id];

        if (timeoutId) {
            clearTimeout(timeoutId);
            delete this.activeToasts[toast.id];
        }
    }

    render() {
        return (
            <div className="toaster">
                <TransitionGroup
                    transitionName="toast"
                    transitionAppear
                    transitionAppearTimeout={300} transitionEnterTimeout={300} transitionLeaveTimeout={300}
                >
                    { this.props.toasts.map((toast) => {
                        if (!toast.id) {
                            // eslint-disable-next-line no-console
                            console.warn("toast cannot be added without 'id' property", toast);
                            return null;
                        }

                        const key = toast.id;

                        return (
                            <Toast
                                key={key}
                                caption={toast.caption}
                                text={toast.text}
                                style={toast.style}
                                action={toast.action}
                                onActionClick={() => this._onActionClick(toast)}
                                onDismiss={() => this._onDismiss(toast)}
                            />
                       );
                    })}
                </TransitionGroup>
            </div>
        );
    }
}

Toaster.propTypes = {
    toasts: PropTypes.array,
    onActionClick: PropTypes.func,
    onDismiss: PropTypes.func,
    dismissDelay: PropTypes.number,
};

Toaster.defaultProps = {
    toasts: [],
};

