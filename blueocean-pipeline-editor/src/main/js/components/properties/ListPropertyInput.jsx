import React from 'react';
import PropTypes from 'prop-types';
import { TextInput } from '@jenkins-cd/design-language';

export default class ListPropertyInput extends React.Component {
    render() {
        return (
            <div>
                <label className="form-label">{this.props.type.capitalizedName + (this.props.type.isRequired ? '*' : '')}</label>
                <div>This property type is not supported</div>
            </div>
        );
    }
}

ListPropertyInput.propTypes = {
    propName: PropTypes.string,
    step: PropTypes.any,
    onChange: PropTypes.func,
    type: PropTypes.any,
};

ListPropertyInput.dataTypes = ['java.util.List'];
