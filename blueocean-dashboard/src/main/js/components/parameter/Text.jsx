import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { TextArea, FormElement } from '@jenkins-cd/design-language';

export class Text extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name } = this.props;
        return (<FormElement title={ name }>
            <TextArea defaultValue={value} />
            { description && <div>{description}</div> }
        </FormElement>);
    }
}

Text.propTypes = propTypes;
