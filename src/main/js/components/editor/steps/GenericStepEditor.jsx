// @flow

import React, { Component, PropTypes } from 'react';
import pipelineMetadataService from '../../../services/PipelineMetadataService';
import debounce from 'lodash.debounce';
import { FormElement, TextInput } from '@jenkins-cd/design-language';

const allPropertyEditors = [
    require('../../properties/BooleanPropertyInput').default,
    require('../../properties/IntegerPropertyInput').default,
    require('../../properties/DecimalPropertyInput').default,
    require('../../properties/StringPropertyInput').default,
    require('../../properties/ListPropertyInput').default,
    require('../../properties/TimeUnitPropertyInput').default,
];

const propertyEditorsByName = {
};

for (let e of allPropertyEditors) {
    for (let t of e.dataTypes) {
        propertyEditorsByName[t] = e;
    }
}

type Props = {
    onChange: Function,
    step: any,
}

type State = {
    stepMetadata: Array<any>,
};

type DefaultProps = typeof GenericStepEditorPanel.defaultProps;

export default class GenericStepEditorPanel extends Component<DefaultProps, Props, State> {
    props:Props;
    state:State;

    constructor(props:Props) {
        super(props);
        this.state = { stepMetadata: null };
    }

    componentWillMount() {
        pipelineMetadataService.getStepListing(stepMetadata => {
            this.setState({stepMetadata: stepMetadata});
        });
    }

    updateStepData = debounce(() => {
        this.props.onChange(this.props.step);
    }, 300);

    render() {
        const { step } = this.props;
        const { stepMetadata } = this.state;

        if (!step || !stepMetadata) {
            return null;
        }

        const thisMeta = stepMetadata.filter(md => md.functionName === step.name)[0];

        return (
            <div className="pipeline-editor-step-generic pipeline-editor-form">
                {thisMeta.parameters.map(p => {
                    const propTypeEditor = propertyEditorsByName[p.type];
                    if (propTypeEditor) {
                        return React.createElement(propTypeEditor, { step: step, type: p, propName: p.name, onChange: () => this.updateStepData() });
                    }
                    return (
                        <FormElement title={p.capitalizedName + (p.isRequired ? '*' : '')} errorMessage={!step.pristine && !step.data[p.name] && (p.capitalizedName + ' is required')}>
                            <TextInput key={p.name} defaultValue={step.data[p.name]} onChange={val => { step.data[p.name] = val; this.updateStepData(); }} />
                        </FormElement>
                    );
                })}
            </div>
        );
    }
}
