import React, { Component } from 'react';
import { TextArea, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';

export class Text extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        const cleanName = removeMarkupTags(name);
        const cleanDescription = removeMarkupTags(description);
        return (
            <FormElement title={cleanDescription}>
                <div className="Text FullWidth">
                    <TextArea {...{ defaultValue: value, name: cleanName, onChange }} />
                </div>
            </FormElement>
        );
    }
}

Text.propTypes = propTypes;
