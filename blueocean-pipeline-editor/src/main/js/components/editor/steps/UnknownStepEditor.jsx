// @flow

import React, { Component, PropTypes } from 'react';
import debounce from 'lodash.debounce';
import { TextInput } from '@jenkins-cd/design-language';
import { convertPipelineStepsToJson, convertJsonStepsToPipeline, convertStepsToJson, convertStepFromJson } from '../../../services/PipelineSyntaxConverter';
import type { PipelineStep } from '../../../services/PipelineSyntaxConverter';

type Props = {
    onChange: Function,
    step: any,
}

type State = {
    stepScript: Array<any>,
};

type DefaultProps = typeof UnknownStepEditorPanel.defaultProps;

export default class UnknownStepEditorPanel extends Component<DefaultProps, Props, State> {
    props:Props;
    state:State;

    constructor(props:Props) {
        super(props);
        this.state = { stepScript: null };
    }

    componentWillMount() {
        convertJsonStepsToPipeline(convertStepsToJson([this.props.step]), stepScript => {
            this.setState({stepScript: stepScript});
        });
    }

    updateStepData = debounce(stepScript => {
        this.setState({stepScript: stepScript});
        convertPipelineStepsToJson(stepScript, (stepJson, errors) => {
            const newStep = convertStepFromJson(stepJson[0]);
            newStep.id = this.props.step.id;
            this.props.onChange(newStep);
        });
    }, 300);

    render() {
        const { step } = this.props;
        const { stepScript } = this.state;

        if (!step || !stepScript) {
            return null;
        }

        return (<textarea className="editor-step-detail-script"
                  defaultValue={stepScript}
                  onChange={(e) => this.updateStepData(e.target.value)} />);
    }
}
