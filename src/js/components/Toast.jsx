import React, {Component, PropTypes} from 'react';

const { func, string } = PropTypes;

export class Toast extends Component {
    onActionClick() {
        console.log('action');
    }

    onDismissClick() {
        console.log('dismiss');
    }
    render() {

        return (
            <div className="toast">
                <span className="text">{this.props.text}</span>
                <a className="action" onClick={() => this.onActionClick()}>{this.props.action}</a>
                <a className="dismiss" onClick={() => this.onDismissClick()}>X</a>
            </div>
        );
    }
}

Toast.propTypes = {
    text: string,
    action: string,
    onActionClick: func,
    onDismiss: func,
};