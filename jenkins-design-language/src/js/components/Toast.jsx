import React, { Component, PropTypes } from 'react';
import { Icon } from './Icon';

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
        const toastStyle = this.props.style || 'default';
        return (
            <div className={`toast ${toastStyle}`}>
                <span className="text">
                    <Caption text={this.props.caption} />
                    {this.props.text}
                </span>
                <a className="action" onClick={() => this.onActionClick()}>
                    {this.props.action}
                </a>
                <a className="dismiss" onClick={() => this.onDismissClick()}>
                    <Icon
                        {...{
                            size: 18,
                            icon: 'ContentClear',
                            style: { fill: '#fff' },
                        }}
                    />
                </a>
            </div>
        );
    }
}

Toast.propTypes = {
    caption: PropTypes.string,
    text: PropTypes.string,
    style: PropTypes.string,
    action: PropTypes.string,
    onActionClick: PropTypes.func,
    onDismiss: PropTypes.func,
};

const Caption = ({ text }) => {
    if (!text) {
        return null;
    }
    return <h4 className="caption">{text}</h4>;
};
Caption.propTypes = {
    text: PropTypes.string,
};
