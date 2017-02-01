import React, { Component } from 'react';
import { Checkbox, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';

export class Boolean extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        const cleanName = removeMarkupTags(name);
        return (<FormElement title={ cleanName }>
            <Checkbox {...{ checked: value, label: removeMarkupTags(description), cleanName, onToggle: onChange }} />
        </FormElement>);
    }
}

Boolean.propTypes = propTypes;
