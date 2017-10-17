import React from 'react';
import { FormElement } from '@jenkins-cd/design-language';
import { getArg, setArg } from '../../services/PipelineMetadataService';


export default class IntegerPropertyInput extends React.Component {
    render() {
        const { type: p, step } = this.props;
        return (
            <FormElement title={p.capitalizedName + (p.isRequired ? '*' : '')}
                errorMessage={!step.pristine && p.isRequired && !getArg(step, p.name).value && (p.capitalizedName + ' is required')}>
                <div className="TextInput">
                    <input type="number" className="TextInput-control" defaultValue={getArg(this.props.step, this.props.propName).value}
                        onChange={e => { setArg(step, this.props.propName, parseInt(e.target.value)); this.props.onChange(step); }}/>
                </div>
            </FormElement>
        );
    }
}

IntegerPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
};

IntegerPropertyInput.dataTypes = [ 'byte', 'short', 'int', 'long', 'java.lang.Byte', 'java.lang.Short', 'java.lang.Integer', 'java.lang.Long', 'java.math.BigInteger' ];
