import React, { Component } from 'react';
import { Dropdown, FormElement, RadioButtonGroup } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';

export class Choice extends Component {

    /**
     * Choose whether to show a dropdown choice or a radioButtonGroup
     * @param choices - alternatives we can present
     */
    radioOrDropDown(choices) {
        if (choices.length > 6) {
            return Dropdown;
        }
        return RadioButtonGroup;
    }

    render() {
        const { defaultParameterValue: { value }, description, name, choices, onChange } = this.props;
        const uxChoice = this.radioOrDropDown(choices);
        const options = {
            defaultOption: value,
            options: choices,
            name,
            onChange,
        };
        const cleanName = removeMarkupTags(name);
        return (<FormElement title={ cleanName }>
            <div className="Choice">
                { React.createElement(uxChoice, { ...options }) }
                { description && <div className="inputDescription">{removeMarkupTags(description)}</div> }
            </div>
        </FormElement>);
    }
}
// <Dropdown {...options} />
// extending the common propType since we have additional properties
// const choicePropTypes = propTypes;
// choicePropTypes.parameters.choices = PropTypes.array;
Choice.propTypes = propTypes;
