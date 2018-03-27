import React, { Component } from 'react';
import { TextInput, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';

export class String extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        const cleanDescription = removeMarkupTags(description);
        const cleanName = removeMarkupTags(name);

        return (
            <FormElement title={cleanDescription || cleanName}>
                <div className="String FullWidth">
                    <TextInput {...{ defaultValue: value, name: cleanName, onChange }} />
                </div>
            </FormElement>
        );
    }
}

String.propTypes = propTypes;
