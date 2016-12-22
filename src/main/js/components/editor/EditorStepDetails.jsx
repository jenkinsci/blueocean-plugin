// @flow

import React, {Component, PropTypes} from 'react';
import pipelineStepListStore from '../../services/PipelineStepListStore';
import type {StepInfo} from './common';
import GenericEditor from './steps/GenericStepEditor';

const allStepEditors = [
    require('./steps/ShellScriptStepEditor').default,
];

const stepEditorsByName = {};

for (let e of allStepEditors) {
    stepEditorsByName[e.stepType] = e;
}

type Props = {
    step?: ?StepInfo,
    onDataChange?: (newValue:any) => void,
    onDeleteStepClick?: (step:StepInfo) => any
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

    componentWillMount() {
        pipelineStepListStore.getStepListing(stepMetadata => {
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
            onDataChange(step.data);
        }
    }

    deleteStepClicked(e: HTMLEvent) {
        e.target.blur(); // Don't leave the button focused

        const {onDeleteStepClick, step} = this.props;

        if (step && onDeleteStepClick) {
            onDeleteStepClick(step);
        }
    }
    
    getStepEditor(step) {
        const editor = stepEditorsByName[step.name];
        if (editor) {
            return editor;
        }
        return GenericEditor;
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
            <div className="editor-step-detail">
                <h4 className="editor-step-detail-label">{step.label}</h4>
                <StepEditor key={step.id} onChange={step => this.commitValue(step)} step={step} />
                <div className="editor-button-bar">
                    <button className="btn-secondary editor-delete-btn" onClick={(e) => this.deleteStepClicked(e)}>Delete step</button>
                </div>
            </div>
        );
    }
}
