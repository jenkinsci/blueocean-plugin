import React, { PropTypes } from 'react';
import { TextControl } from './TextControl';

export class TextArea extends React.Component {
    render() {
        return (
            <TextControl {...this.props} className={`TextArea ${this.props.className}`}>
                <textarea className="TextArea-control" name={this.props.name} />
            </TextControl>
        );
    }
}

TextArea.propTypes = {
    className: PropTypes.string,
    placeholder: PropTypes.string,
    name: PropTypes.string,
    defaultValue: PropTypes.string,
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
};

TextArea.defaultProps = {
    className: '',
};
