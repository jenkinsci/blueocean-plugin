import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { Dropdown, FormElement } from '@jenkins-cd/design-language';

export class Choice extends Component {

    render() {
        const { defaultParameterValue: { value }, description, name, choices } = this.props;
        return (<FormElement title={ name }>
            <Dropdown defaultOption={value} options={choices} />
            { description && <div>{description}</div> }
        </FormElement>); }
}

Choice.propTypes = propTypes;
