import React, { PropTypes } from 'react';
import { SvgStatus, SvgSpinner } from '@jenkins-cd/design-language';


// must equal .Button-svg: transition + transition-delay
const SUCCESS_DURATION = 2250;


// TODO: migrate to JDL and merge w/ IconButton
export class Button extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            success: false,
        };
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.progress && !nextProps.progress) {
            this.setState({
                success: true,
            });

            this.timeoutId = setTimeout(() => {
                this.setState({
                    success: false,
                });
            }, SUCCESS_DURATION);
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
            progress,
        } = this.props;

        const radius = 12;
        const transforms = [`translate(${radius} ${radius})`];

        let status = '';

        if (progress) {
            status = 'running';
        } else if (this.state.success) {
            status = 'success';
            transforms.push('scale(2)');
        }

        const progressClass = status ? 'Button-progress' : '';
        const successClass = this.state.success ? 'Button-success' : '';
        const disabled = !!status;

        return (
            <button
                className={`Button ${className} ${progressClass} ${successClass}`}
                style={style}
                disabled={disabled}
                onClick={() => this._onClick()}
            >
                <span className="Button-text">{ children }</span>

                { status &&
                    <svg className="Button-svg" xmlns="http://www.w3.org/2000/svg"
                         viewBox={`0 0 ${radius * 2} ${radius * 2}`} width={16} height={16}
                    >
                        <title>{status}</title>

                        <g transform={transforms.join(' ')}>
                            { status === 'running' &&
                                <SvgSpinner radius={radius} result={status} percentage={999} />
                            }
                            { status === 'success' &&
                                <SvgStatus radius={radius} result={status} />
                            }
                        </g>
                    </svg>
                }
            </button>
        );
    }
}

Button.propTypes = {
    className: PropTypes.string,
    style: PropTypes.object,
    children: PropTypes.string,
    progress: PropTypes.bool,
    onClick: PropTypes.func,
};
