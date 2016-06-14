import React, {Component, PropTypes} from 'react';
import ReactDOM from 'react-dom';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import {Icon} from 'react-material-icons-blue';

const { func, number, string } = PropTypes;

/**
 * Toast displays a small confirmation message with an optional action link and dismiss link.
 * Toast will auto-dismiss itself after 5s.
 *
 * Supported props:
 * text - Confirmation message text, e.g. "Run Started"
 * action - Optional action link, e.g. "Open"
 * onActionClick - function to invoke when action link is clicked
 * onDismiss - function to invoke when dismiss link is clicked, or Toast auto-dismisses.
 * dismissDelay - Duration in millis before dialog dismisses itself. default is 5000
 */
export class Toast extends Component {
    constructor() {
        super();
        this.state = {
            dismissing: false
        };
        this._dismissingTimeoutID = 0;
        this._destroyingTImeoutID = 0;
    }

    componentDidMount() {
        // disable the auto-dismiss when delay set explicitly to zero
        if (this.props.dismissDelay === 0) {
            return;
        }

        const delay = this.props.dismissDelay || 5000;

        // automatically dismiss the component after specified delay
        this._dismissingTimeoutID = setTimeout(() => {
            this.callDismissListener();
            this.hideComponent();
        }, delay);
    }

    componentWillUnmount() {
        clearTimeout(this._dismissingTimeoutID);
        clearTimeout(this._destroyingTImeoutID);
    }

    onActionClick() {
        if (this.props.onActionClick) {
            this.props.onActionClick();
        }

        this.hideComponent();
    }

    onDismissClick() {
        this.callDismissListener();
        this.hideComponent();
    }

    callDismissListener() {
        if (this.props.onDismiss) {
            this.props.onDismiss();
        }
    }

    hideComponent() {
        this.setState({
            dismissing: true
        });

        clearTimeout(this._dismissingTimeoutID);
        this._destroyingTImeoutID = setTimeout(() => this.destroyComponent(), 300);
    }

    destroyComponent() {
        const element = ReactDOM.findDOMNode(this);
        ReactDOM.unmountComponentAtNode(element);
    }

    render() {
        return (
            <ReactCSSTransitionGroup transitionName="toast" transitionAppear
                transitionAppearTimeout={300} transitionEnterTimeout={300} transitionLeaveTimeout={300}
            >
                { !this.state.dismissing ?
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
                : null }
            </ReactCSSTransitionGroup>
        );
    }
}

Toast.propTypes = {
    text: string,
    action: string,
    onActionClick: func,
    onDismiss: func,
    dismissDelay: number,
};
