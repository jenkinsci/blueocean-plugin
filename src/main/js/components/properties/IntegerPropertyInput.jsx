import React from 'react';
import { FormElement } from '@jenkins-cd/design-language';

export default class IntegerPropertyInput extends React.Component {
    render() {
        const { type: p, step } = this.props;
        return (
            <FormElement title={p.capitalizedName + (p.isRequired ? '*' : '')}
                errorMessage={!step.pristine && p.isRequired && !step.data[p.name] && (p.capitalizedName + ' is required')}>
                <div className="TextInput">
                    <input type="number" className="TextInput-control" defaultValue={this.props.step.data[this.props.propName]}
                        onChange={e => { step.data[this.props.propName] = parseInt(e.target.value); this.props.onChange(step); }}/>
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
