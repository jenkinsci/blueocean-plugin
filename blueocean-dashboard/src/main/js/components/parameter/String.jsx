import React, { Component } from 'react';
import { TextInput, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from './commonProptypes';

export class String extends Component {

    render() {
        const { defaultParameterValue: { value }, description, name } = this.props;
        return (<FormElement title={ name }>
            <TextInput defaultValue={value} />
            { description && <div>{description}</div> }
        </FormElement>);
    }
}

String.propTypes = propTypes;
