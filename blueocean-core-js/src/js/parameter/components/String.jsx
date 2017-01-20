import React, { Component } from 'react';
import { TextInput, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';

export class String extends Component {

    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        return (<FormElement title={ name }>
            <div className="String">
                <TextInput {...{ defaultValue: value, name, onChange }} />
                { description && <div className="inputDescription">{description}</div> }
            </div>
        </FormElement>);
    }
}

String.propTypes = propTypes;
