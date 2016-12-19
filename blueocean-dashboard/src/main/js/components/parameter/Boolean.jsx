import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { Checkbox, FormElement } from '@jenkins-cd/design-language';

export class Boolean extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name } = this.props;
        return (<FormElement title={ name }>
            <Checkbox label={description} checked={value} />
        </FormElement>);
    }
}

Boolean.propTypes = propTypes;
