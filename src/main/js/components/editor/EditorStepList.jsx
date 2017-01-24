// @flow

import React, { Component, PropTypes } from 'react';
import { getAddIconGroup, getGrabIconGroup } from './common';
import pipelineMetadataService from '../../services/PipelineMetadataService';
import type { StepInfo } from '../../services/PipelineStore';
import { Icon } from "@jenkins-cd/react-material-icons";

type Props = {
    steps: Array<StepInfo>,
    selectedStep?: ?StepInfo,
    onAddStepClick?: () => any,
    onStepSelected?: (step:StepInfo) => any,
    onAddChildStepClick?: (step:StepInfo) => any,
}

type State = {
    selectedStep: ?StepInfo
};

type DefaultProps = typeof EditorStepList.defaultProps;

export class EditorStepList extends Component<DefaultProps, Props, State> {

    static defaultProps = {
        steps: []
    };

    //static propTypes = {...}
    // TODO: React proptypes ^^^

    props:Props;
    state:State;

    constructor(props:Props) {
        super(props);
        this.state = {selectedStep: props.selectedStep};
    }

    componentWillMount() {
        pipelineMetadataService.getStepListing(stepMetadata => {
            this.setState({stepMetadata: stepMetadata});
        });
    }

    componentWillReceiveProps(nextProps:Props) {
        if (nextProps.selectedStep !== this.props.selectedStep) {
            this.setState({selectedStep: nextProps.selectedStep});
        }
    }

    renderStep(step:StepInfo, selectedStep:?StepInfo, isChild: boolean = false) {

        let classNames = ["editor-step"];

        if (step === selectedStep) {
            classNames.push("selected");
        }

        let children = null;

        if (step.isContainer && step.children && step.children.length) {
            children = (
                <div className="editor-nested-steps">
                    { step.children.map(step => this.renderStep(step, selectedStep, true)) }
                </div>
            );
        }

        const addStepButton = (step.isContainer) ? (
            <div className="editor-button-bar">
                <button className="btn-primary add"
                        onClick={(e) => this.addChildStepClicked(step, e)}>
                    <Icon icon="add" size={20} />
                    Add Step
                </button>
            </div>
        ) : null;

        if (!this.state.stepMetadata) {
            return;
        }
        const thisMeta = this.state.stepMetadata.filter(md => md.functionName === step.name)[0];

        return (
            <div className={classNames.join(' ')} key={'s_' + step.id}>
                <div className="editor-step-main" onClick={(e) => this.stepClicked(step, e)}>
                    <div className="editor-step-content">
                        {isChild && <div className="editor-step-child-icon">
                            <svg fill="#000000" height="16" viewBox="0 0 24 24" width="16" xmlns="http://www.w3.org/2000/svg">
                                <path d="M0 0h24v24H0V0z" fill="none"/>
                                <path d="M19 15l-6 6-1.42-1.42L15.17 16H4V4h2v10h9.17l-3.59-3.58L13 9l6 6z"/>
                            </svg>
                        </div>}
                        <div className="editor-step-title">
                            <span className="editor-step-label">{step.label}</span>
                            <span className="editor-step-summary">
                            {thisMeta.parameters.filter(p => p.isRequired).map(p =>
                                step.data[p.name]
                            )}
                            </span>
                        </div>
                    </div>

                    {children}
                    {addStepButton}
                </div>
            </div>
        );
    }

    stepClicked(step:StepInfo, e:HTMLEvent) {
        e.stopPropagation(); // Don't bubble up to parent

        const {onStepSelected} = this.props;

        if (step !== this.state.selectedStep) {
            this.setState({selectedStep: step});
            if (onStepSelected) {
                onStepSelected(step);
            }
        }
    }

    addStepClicked(e: HTMLEvent) {
        e.target.blur(); // Don't leave the button focused
        const {onAddStepClick} = this.props;
        if (onAddStepClick) {
            onAddStepClick();
        }
    }

    addChildStepClicked(parent:StepInfo, e:HTMLEvent) {
        e.stopPropagation(); // Don't bubble up to parent
        e.target.blur(); // Don't leave the button focused
        const {onAddChildStepClick} = this.props;
        if (onAddChildStepClick) {
            onAddChildStepClick(parent);
        }
    }

    render() {
        const {steps} = this.props;
        const {selectedStep} = this.state;

        return (
            <div className="editor-steps">
                { steps.map(step => this.renderStep(step, selectedStep)) }
                <div className="editor-button-bar">
                    <button className="btn-primary add" onClick={(e) => this.addStepClicked(e)}>
                        <Icon icon="add" size={20} />
                        Add step
                    </button>
                </div>
            </div>
        );
    }
}
