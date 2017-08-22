import React from 'react';
import { Checkbox } from '@jenkins-cd/design-language';

export default class BooleanPropertyInput extends React.Component {
    render() {
        return (
            <div>
                <Checkbox checked={this.props.step.data[this.props.propName]}
                    onToggle={checked => { this.props.step.data[this.props.propName] = checked; this.props.onChange(this.props.step); }}
                    label={this.props.type.capitalizedName + (this.props.type.isRequired ? '*' : '')} />
            </div>
        );
    }
}

BooleanPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
};

BooleanPropertyInput.dataTypes = [ 'boolean', 'java.lang.Boolean' ];
