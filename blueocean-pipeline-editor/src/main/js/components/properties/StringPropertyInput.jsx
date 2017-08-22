import React from 'react';
import { FormElement, TextInput } from '@jenkins-cd/design-language';

export default class StringPropertyInput extends React.Component {
    render() {
        const { type: p, step } = this.props;
        return (
            <FormElement title={p.capitalizedName + (p.isRequired ? '*' : '')}
                errorMessage={!step.pristine && p.isRequired && !step.data[p.name] && (p.capitalizedName + ' is required')}>
                <TextInput defaultValue={step.data[this.props.propName]}
                    onChange={val => { step.data[this.props.propName] = val; this.props.onChange(step); }}/>
            </FormElement>
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
