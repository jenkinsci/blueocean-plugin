import React, { PropTypes } from 'react';

/**
 * Contains common logic for text-based controls.
 * Not intended to be used directly.
 */
export class TextControl extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            value: '',
        };
    }

    componentWillMount() {
        this._initialize(this.props);
    }

    get value() {
        return this.state.value;
    }

    _initialize(props) {
        if (props.defaultValue) {
            this.setState({
                value: props.defaultValue,
            });
        }
    }

    _onChange(event) {
        const { value } = event.currentTarget;

        this.setState({
            value,
        });

        if (this.props.onChange) {
            this.props.onChange(value);
        }
    }

    _onBlur(event) {
        const { value } = event.currentTarget;

        if (this.props.onBlur) {
            this.props.onBlur(value);
        }
    }

    render() {
        return (
            <div className={this.props.className}>
                {React.Children.map(this.props.children, child => {
                    // while multiple children can be passed in (icons, etc)
                    // we only want to pass down props to underlying text control
                    if (child && (child.type === 'input' || child.type === 'textarea')) {
                        return React.cloneElement(child, {
                            placeholder: this.props.placeholder,
                            disabled: this.props.disabled,
                            value: this.state.value,
                            onChange: e => this._onChange(e),
                            onBlur: e => this._onBlur(e),
                        });
                    }

                    return child;
                })}
            </div>
        );
    }
}

TextControl.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
};

TextControl.defaultProps = {
    className: 'TextControl',
};
