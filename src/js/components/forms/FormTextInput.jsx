/**
 * Created by cmeyers on 11/1/16.
 */
import React, { PropTypes } from 'react';
import FormElement from './FormElement';
import TextInput from './TextInput';

export default class FormTextInput extends React.Component {

    constructor(props) {
        super(props);

        this.input = null;

        this.state = {
            errorMessage: '',
        };
    }

    _validate(value) {
        const errorMessage = value === '' ? 'This field is required.' : '';

        this.setState({
            errorMessage,
        });
    }

    _onChange(value) {
        this._validate(value);

        if (this.props.onChange) {
            this.props.onChange(value);
        }
    }

    _onBlur() {
        this._validate(this.input.value);
    }

    render() {
        return (
            <FormElement errorMessage={this.state.errorMessage}>
                <TextInput
                  ref={input => this.input = input}
                  placeholder={this.props.placeholder}
                  defaultValue={this.props.defaultValue}
                  onChange={debounce(val => this._onChange(val), 250)}
                  onBlur={() => this._onBlur()}
                />
            </FormElement>
        );
    }

}

FormTextInput.propTypes = {
    className: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    onChange: PropTypes.string,
};

/* eslint-disable */
function debounce(func, wait, immediate) {
    var timeout;
    return function() {
        var context = this, args = arguments;
        var later = function() {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        var callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func.apply(context, args);
    };
};
/* eslint-enable */
