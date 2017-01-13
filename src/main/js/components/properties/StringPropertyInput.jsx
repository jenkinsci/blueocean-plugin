import React from 'react';
import { TextInput } from '@jenkins-cd/design-language';

export default class StringPropertyInput extends React.Component {
    render() {
        return (
            <div>
                <label className="form-label">{this.props.type.capitalizedName}</label>
                <TextInput defaultValue={this.props.step.data[this.props.propName]}
                    onChange={val => { this.props.step.data[this.props.propName] = val; this.props.onChange(this.props.step); }}/>
            </div>
        );
    }
}

StringPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
    type: React.PropTypes.any,
};

StringPropertyInput.dataTypes = [ 'java.lang.String' ];
