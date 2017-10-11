import React, { PropTypes } from 'react';
import { StatusIndicator } from '@jenkins-cd/design-language';

// must equal .Button-icon: transition + transition-delay
const ANIMATION_DURATION = 2250;

// TODO: migrate to JDL and merge w/ IconButton

/**
 * Button control that supports a "status" icon for pending operations and result.
 * Driven by 'status' prop. Examples:
 *
 * to display "pending" state (until subsequent 'status' update)
 * {
 *     result: 'running',
 * }
 *
 * to display success feedback which resets to button text after 2s
 * {
 *     result: 'success',
 *     reset: true,
 * }
 */
export class Button extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            result: null,
            pendingReset: false,
        };
    }

    componentWillReceiveProps(nextProps) {
        const statusChanged =
            !this.props.status || !nextProps.status || this.props.status.result !== nextProps.status.result || this.state.result !== nextProps.status.result;

        const shouldReset = nextProps.status && nextProps.status.reset;

        if (statusChanged) {
            const result = nextProps.status && nextProps.status.result;
            this.setState({
                result,
                pendingReset: shouldReset,
            });
        }

        // schedule a reset of internal "state.result" to restore button text
        if (statusChanged && shouldReset) {
            this.timeoutId = setTimeout(() => {
                this.setState({
                    result: null,
                    pendingReset: false,
                });
            }, ANIMATION_DURATION);
        }
    }

    componentWillUnmount() {
        this._cancelTimeout();
    }

    timeoutId = 0;

    _cancelTimeout() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
            this.timeoutId = 0;
        }
    }

    _onClick() {
        if (this.props.onClick) {
            this.props.onClick();
        }
    }

    render() {
        const { className = '', style = {}, children } = this.props;

        const { result } = this.state;
        const disabled = result === 'running' || this.props.disabled;
        const statusClass = result ? 'Button-status' : '';
        const transitionClass = this.state.pendingReset ? 'Button-transitioning' : '';

        return (
            <button className={`Button ${className} ${statusClass} ${transitionClass}`} style={style} disabled={disabled} onClick={() => this._onClick()}>
                <span className="Button-text">{children}</span>

                <div className="Button-icon">{result && <StatusIndicator result={result} percentage={999} width={16} height={16} noBackground />}</div>
            </button>
        );
    }
}

Button.propTypes = {
    className: PropTypes.string,
    style: PropTypes.object,
    children: PropTypes.string,
    disabled: PropTypes.bool,
    status: PropTypes.shape({
        result: PropTypes.string,
        reset: PropTypes.bool,
    }),
    onClick: PropTypes.func,
};
