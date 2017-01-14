import React, { Component } from 'react';
import { TextArea, FormElement } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';

export class Text extends Component {
    render() {
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        return (<FormElement title={ name }>
            <div className="Text">
                <TextArea {...{ defaultValue: value, name, onChange }} />
                { description && <div className="inputDescription">{description}</div> }
            </div>
        </FormElement>);
    }
}

Text.propTypes = propTypes;
