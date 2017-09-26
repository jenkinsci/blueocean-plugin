import React, { PropTypes } from 'react';
import { Icon } from '../Icon';

import { TextControl } from './TextControl';


// wraps the Icon in a div to provide consistent cursor behavior
function NestedIcon(props) {
    return (
        <div className={`TextInput-icon ${props.className}`}>
            <Icon icon={props.icon} />
        </div>
    );
}

NestedIcon.propTypes = {
    className: PropTypes.string,
    icon: PropTypes.string,
};


export class TextInput extends React.Component {

    render() {
        const classLeft = this.props.iconLeft ? 'u-icon-left' : '';
        const classRight = this.props.iconRight ? 'u-icon-right': '';
        const { ariaLabel = this.props.placeholder } = this.props;

        return (
            <TextControl {...this.props} className={`TextInput ${this.props.className} ${classLeft} ${classRight}`}>
                { classLeft && <NestedIcon className={classLeft} icon={this.props.iconLeft} /> }
                <input aria-label={ ariaLabel } type="text" className="TextInput-control" { ...{ name: this.props.name } } />
                { classRight && <NestedIcon className={classRight} icon={this.props.iconRight} /> }
            </TextControl>
        );
    }

}

TextInput.propTypes = {
    className: PropTypes.string,
    name: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    disabled: PropTypes.bool,
    iconLeft: PropTypes.string,
    iconRight: PropTypes.string,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
    ariaLabel: PropTypes.string
};

TextInput.defaultProps = {
    className: '',
};
