import React, { PropTypes } from 'react';

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
                { React.cloneElement(
                    this.props.children,
                    {
                        placeholder: this.props.placeholder,
                        value: this.state.value,
                        onChange: e => this._onChange(e),
                        onBlur: e => this._onBlur(e),
                    }
                ) }
            </div>
        );
    }

}

TextControl.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
};

TextControl.defaultProps = {
    className: 'TextControl',
};
