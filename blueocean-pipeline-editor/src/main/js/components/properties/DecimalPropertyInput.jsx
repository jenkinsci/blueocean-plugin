import React from 'react';
import { FormElement } from '@jenkins-cd/design-language';

export default class DecimalPropertyInput extends React.Component {
    render() {
        const { type: p, step } = this.props;
        return (
            <FormElement title={p.capitalizedName + (p.isRequired ? '*' : '')}
                errorMessage={!step.pristine && p.isRequired && !step.data[p.name] && (p.capitalizedName + ' is required')}>
                <div className="TextInput">
                    <input type="number" className="TextInput-control" defaultValue={this.props.step.data[this.props.propName]}
                        onChange={e => { this.props.step.data[this.props.propName] = parseFloat(e.target.value); this.props.onChange(this.props.step); }}/>
                </div>
            </FormElement>
        );
    }
}

DecimalPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
};

DecimalPropertyInput.dataTypes = [ 'float', 'double', 'java.lang.Float', 'java.lang.Double', 'java.math.BigDecimal' ];
