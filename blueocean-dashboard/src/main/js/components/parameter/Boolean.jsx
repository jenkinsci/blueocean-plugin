import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { Checkbox, FormElement } from '@jenkins-cd/design-language';

export class Boolean extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name } = this.props;
        return (<FormElement title={ name }>
            <Checkbox {...{ checked: value, label: description, name }} />
        </FormElement>);
    }
}

Boolean.propTypes = propTypes;
