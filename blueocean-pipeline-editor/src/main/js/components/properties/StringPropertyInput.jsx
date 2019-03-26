import React from 'react';
import PropTypes from 'prop-types';
import { FormElement, TextInput } from '@jenkins-cd/design-language';
import { getArg, setArg } from '../../services/PipelineMetadataService';

export default class StringPropertyInput extends React.Component {
    render() {
        const { type: p, step } = this.props;
        return (
            <FormElement
                title={p.capitalizedName + (p.isRequired ? '*' : '')}
                errorMessage={!step.pristine && p.isRequired && !getArg(step, p.name).value && p.capitalizedName + ' is required'}
            >
                <TextInput
                    defaultValue={getArg(step, this.props.propName).value}
                    onChange={val => {
                        setArg(step, this.props.propName, val);
                        this.props.onChange(step);
                    }}
                />
            </FormElement>
        );
    }
}

StringPropertyInput.propTypes = {
    propName: PropTypes.string,
    step: PropTypes.any,
    onChange: PropTypes.func,
    type: PropTypes.any,
};

StringPropertyInput.dataTypes = ['java.lang.String'];
