import React, {Component, PropTypes} from 'react';
import {Icon} from '@jenkins-cd/react-material-icons';

/**
 * Toast displays a small confirmation message with an optional action link and dismiss link.
 * Toast will auto-dismiss itself after 5s.
 *
 * Supported props:
 * text - Confirmation message text, e.g. "Run Started"
 * action - Optional action link, e.g. "Open"
 * onActionClick - function to invoke when action link is clicked
 * onDismiss - function to invoke when dismiss link is clicked, or Toast auto-dismisses.
 */
export class Toast extends Component {

    onActionClick() {
        if (this.props.onActionClick) {
            this.props.onActionClick();
        }
    }

    onDismissClick() {
        if (this.props.onDismiss) {
            this.props.onDismiss();
        }
    }

    render() {
        return (
            <div className="toast">
                <span className="text">{this.props.text}</span>
                <a className="action" onClick={() => this.onActionClick()}>{this.props.action}</a>
                <a className="dismiss" onClick={() => this.onDismissClick()}>
                  <Icon {...{
                      size: 18,
                      icon: 'clear',
                      style: { fill: "#fff" },
                  }} />
                </a>
            </div>
        );
    }
}

Toast.propTypes = {
    text: PropTypes.string,
    action: PropTypes.string,
    onActionClick: PropTypes.func,
    onDismiss: PropTypes.func,
};
