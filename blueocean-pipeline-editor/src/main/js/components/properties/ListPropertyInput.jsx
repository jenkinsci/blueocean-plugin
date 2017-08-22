import React from 'react';
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
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
    type: React.PropTypes.any,
};

ListPropertyInput.dataTypes = [ 'java.util.List' ];
