import React from 'react';
import PropTypes from 'prop-types';
import { Checkbox } from '@jenkins-cd/design-language';
import { getArg, setArg } from '../../services/PipelineMetadataService';

export default class BooleanPropertyInput extends React.Component {
    render() {
        return (
            <div>
                <Checkbox
                    checked={getArg(this.props.step, this.props.propName).value}
                    onToggle={checked => {
                        setArg(this.props.step, this.props.propName, checked);
                        this.props.onChange(this.props.step);
                    }}
                    label={this.props.type.capitalizedName + (this.props.type.isRequired ? '*' : '')}
                />
            </div>
        );
    }
}

BooleanPropertyInput.propTypes = {
    propName: PropTypes.string,
    step: PropTypes.any,
    onChange: PropTypes.func,
};

BooleanPropertyInput.dataTypes = ['boolean', 'java.lang.Boolean'];
