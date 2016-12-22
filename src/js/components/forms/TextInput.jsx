import React, { PropTypes } from 'react';
import { TextControl } from './TextControl';

export class TextInput extends React.Component {

    render() {
        return (
            <TextControl {...this.props} className={`TextInput ${this.props.className}`}>
                <input type="text" className="TextInput-control"/>
            </TextControl>
        );
    }

}

TextInput.propTypes = {
    className: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
};

TextInput.defaultProps = {
    className: '',
};
