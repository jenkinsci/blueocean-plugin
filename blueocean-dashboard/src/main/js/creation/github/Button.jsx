import React, { PropTypes } from 'react';
import { StatusIndicator } from '@jenkins-cd/design-language';


// must equal .Button-svg: transition + transition-delay
const ANIMATION_DURATION = 2250;


// TODO: migrate to JDL and merge w/ IconButton
export class Button extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            result: null,
            transition: false,
        };
    }

    componentWillReceiveProps(nextProps) {
        // TODO: probably need to check against state's result instead of props?
        const statusChanged = !this.props.status || !nextProps.status ||
            this.props.status.result !== nextProps.status.result;
        const resetAfterDelay = nextProps.status && nextProps.status.reset;

        console.log(`statusChanged: ${statusChanged}, resetAfterDelay ${resetAfterDelay}`);

        if (statusChanged) {
            const result = nextProps.status && nextProps.status.result;
            this.setState({
                result,
                transition: resetAfterDelay,
            });
        }

        if (statusChanged && resetAfterDelay) {
            this.timeoutId = setTimeout(() => {
                this.setState({
                    result: null,
                    transition: false,
                });
            }, ANIMATION_DURATION);
        }
    }

    componentWillUnmount() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
            this.timeoutId = 0;
        }
    }

    timeoutId = 0;

    _onClick() {
        if (this.props.onClick) {
            this.props.onClick();
        }
    }

    render() {
        const {
            className = '',
            style = {},
            children,
        } = this.props;

        const { result } = this.state;
        const disabled = result === 'running';
        const statusClass = result ? 'Button-status' : '';
        const transitionClass = this.state.transition ? 'Button-transitioning' : '';

        return (
            <button
                className={`Button ${className} ${statusClass} ${transitionClass}`}
                style={style}
                disabled={disabled}
                onClick={() => this._onClick()}
            >
                <span className="Button-text">{ children }</span>

                <div className="Button-icon">
                { result &&
                    <StatusIndicator
                        result={result}
                        percentage={999}
                        width={16}
                        height={16}
                        noBackground
                    />
                }
                </div>
            </button>
        );
    }
}

Button.propTypes = {
    className: PropTypes.string,
    style: PropTypes.object,
    children: PropTypes.string,
    status: PropTypes.shape({
        result: PropTypes.string,
        reset: PropTypes.bool,
    }),
    onClick: PropTypes.func,
};
