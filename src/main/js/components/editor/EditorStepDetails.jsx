// @flow

import React, {Component, PropTypes} from 'react';
import pipelineMetadataService from '../../services/PipelineMetadataService';
import type { StepInfo } from '../../services/PipelineStore';
import GenericStepEditor from './steps/GenericStepEditor';
import UnknownStepEditor from './steps/UnknownStepEditor';
import { EditorStepList } from './EditorStepList';
import { ValidationMessageList } from './ValidationMessageList';

const allStepEditors = [
    require('./steps/ShellScriptStepEditor').default,
    require('./steps/PipelineScriptStepEditor').default,
];

const stepEditorsByName = {};

for (let e of allStepEditors) {
    stepEditorsByName[e.stepType] = e;
}

type Props = {
    step?: ?StepInfo,
    onDataChange?: (newValue:any) => void,
}

export class EditorStepDetails extends Component {
    props:Props;

    state:{
        step: string,
        stepMetadata: any,
    };

    static defaultProps = {};

    constructor(props:Props) {
        super(props);
    }

    getTitle() {
        return this.props.step.label;
    }

    componentWillMount() {
        pipelineMetadataService.getStepListing(stepMetadata => {
            this.setState({stepMetadata: stepMetadata});
        });
    }

    componentWillReceiveProps(nextProps:Props) {
        if (nextProps.step !== this.props.step) {
            this.setState({step: nextProps.step});
        }
    }

    commitValue(step) {
        const {onDataChange} = this.props;
        if (onDataChange) {
            onDataChange(step);
        }
    }

    getStepMetadata(step) {
        const meta = this.state.stepMetadata.filter(md => md.functionName === step.name);
        if (meta && meta.length) {
            return meta[0];
        }
        return null;
    }

    getStepEditor(step) {
        const editor = stepEditorsByName[step.name];
        if (editor) {
            return editor;
        }
        if (!this.state.stepMetadata) {
            return null;
        }

        if (!this.getStepMetadata(step)) {
            return UnknownStepEditor;
        }
        return GenericStepEditor;
    }

    render() {

        const {step} = this.props;

        if (!step) {
            return (
                <div className="editor-step-detail no-step">
                    <p>Select or create a step</p>
                </div>
            );
        }
        
        const StepEditor = this.getStepEditor(step);

        return (
            <div className="editor-step-detail editor-config-panel">
                <section>
                    <ValidationMessageList node={step} />
                    <StepEditor key={step.id} onChange={step => this.commitValue(step)} step={step} />
                </section>
                {step.isContainer && <section>
                    <h5>Child steps</h5>
                    <EditorStepList steps={step.children}
                        parent={step}
                        onAddStepClick={() => this.props.openSelectStepDialog(step)}
                        onStepSelected={(step) => this.props.selectedStepChanged(step)} />
                </section>}
            </div>
        );
    }
}
