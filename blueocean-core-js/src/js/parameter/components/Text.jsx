import React, { Component } from 'react';
import { TextArea, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';

export class Text extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        const cleanName = removeMarkupTags(name);
        return (<FormElement title={ cleanName }>
            <div className="Text">
                <TextArea {...{ defaultValue: value, cleanName, onChange }} />
                { description && <div className="inputDescription">{removeMarkupTags(description)}</div> }
            </div>
        </FormElement>);
    }
}

Text.propTypes = propTypes;
