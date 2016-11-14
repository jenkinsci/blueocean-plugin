/**
 * Created by cmeyers on 11/1/16.
 */
import React, { PropTypes } from 'react';
import debounce from 'lodash.debounce';

import {FormElement} from './FormElement';
import {TextInput} from './TextInput';

export class FormTextInput extends React.Component {

    constructor(props) {
        super(props);

        this.input = null;

        this.state = {
            errorMessage: '',
        };
    }

    _validate(value) {
        const errorMessage = value === '' ? 'required' : '';

        this.setState({
            errorMessage,
        });
    }

    _onChange = debounce((value) => {
        this._validate(value);

        if (this.props.onChange) {
            this.props.onChange(value);
        }
    }, 250);

    _onBlur() {
        this._validate(this.input.value);
    }

    render() {
        return (
            <FormElement title={this.props.title} errorMessage={this.state.errorMessage}>
                <TextInput
                  ref={input => this.input = input}
                  placeholder={this.props.placeholder}
                  defaultValue={this.props.defaultValue}
                  onChange={this._onChange}
                  onBlur={() => this._onBlur()}
                />
            </FormElement>
        );
    }

}

FormTextInput.propTypes = {
    className: PropTypes.string,
    title: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    onChange: PropTypes.string,
};
