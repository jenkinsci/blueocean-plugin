import React, { PropTypes } from 'react';
import { TextControl } from './TextControl';

export class PasswordInput extends React.Component {
    render() {
        return (
            <TextControl {...this.props} className={`PasswordInput ${this.props.className}`}>
                <input type="password" className="TextInput-control" {...{ name: this.props.name }} />
            </TextControl>
        );
    }
}

PasswordInput.propTypes = {
    className: PropTypes.string,
    name: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
};

PasswordInput.defaultProps = {
    className: '',
};
