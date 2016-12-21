import React, { Component } from 'react';
import { propTypes } from './commonProptypes';
import { TextArea, FormElement } from '@jenkins-cd/design-language';

export class Text extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        return (<FormElement title={ name }>
            <div>
                <TextArea {...{ defaultValue: value, name, onChange }} />
                { description && <div>{description}</div> }
            </div>
        </FormElement>);
    }
}

Text.propTypes = propTypes;
