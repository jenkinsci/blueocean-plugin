import React, { Component } from 'react';
import { TextInput, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';

export class String extends Component {

    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        const cleanName = removeMarkupTags(name);
        return (<FormElement title={ cleanName }>
            <div className="String">
                <TextInput {...{ defaultValue: value, cleanName, onChange }} />
                { description && <div className="inputDescription">{removeMarkupTags(description)}</div> }
            </div>
        </FormElement>);
    }
}

String.propTypes = propTypes;
