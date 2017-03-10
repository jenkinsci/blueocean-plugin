// @flow

import React, { Component, PropTypes } from 'react';
import { getAddIconGroup, getGrabIconGroup } from './common';
import pipelineMetadataService from '../../services/PipelineMetadataService';
import type { StepInfo } from '../../services/PipelineStore';
import { Icon } from "@jenkins-cd/react-material-icons";
import pipelineValidator from '../../services/PipelineValidator';

type Props = {
    steps: Array<StepInfo>,
    parent: ?StepInfo,
    onAddStepClick?: () => any,
    onStepSelected?: (step:StepInfo) => any,
    onAddChildStepClick?: (step:StepInfo) => any,
}

type State = {};

type DefaultProps = typeof EditorStepList.defaultProps;

function ChildStepIcon() {
    return (<div className="editor-step-child-icon">
        <svg fill="#000000" height="16" viewBox="0 0 24 24" width="16" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 0h24v24H0V0z" fill="none"/>
            <path d="M19 15l-6 6-1.42-1.42L15.17 16H4V4h2v10h9.17l-3.59-3.58L13 9l6 6z"/>
        </svg>
    </div>);
}

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
        this.state = {};
    }

    componentWillMount() {
        pipelineMetadataService.getStepListing(stepMetadata => {
            this.setState({stepMetadata: stepMetadata});
        });
    }

    renderSteps(steps: StepInfo[], parent: ?StepInfo) {
        if (!steps.length) {
            if(parent) {
                // if no children, render a placeholder to show this is a container
                return (
                    <div className="editor-step nested missing">
                        <div className="editor-step-main" onClick={(e) => this.stepClicked(parent, e)}>
                            <div className="editor-step-content">
                                <ChildStepIcon/>
                                <div className="editor-step-title">
                                    <span className="editor-step-summary">
                                        There are no child steps defined
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>);
            } else {
                // we know there will be a validation error in this case, show
                // something indicating the user needs a step in a stage
                return (
                    <div className="editor-step missing">
                        <div className="editor-step-main">
                            <div className="editor-step-content">
                                <ChildStepIcon/>
                                <div className="editor-step-title">
                                    <span className="editor-step-summary">
                                        There are no steps, at least one is required.
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>);
            }
        }
        
        return steps.map(step => this.renderStep(step, parent));
    }

    renderStep(step:StepInfo, parent: StepInfo) {
        const thisMeta = this.state.stepMetadata.find(step);
        const classNames = ["editor-step"];
        const errors = pipelineValidator.getNodeValidationErrors(step);
        if (parent) classNames.push('nested');
        if (errors) classNames.push('errors');

        return (
            <div className={classNames.join(' ')} key={'s_' + step.id}>
                <div className="editor-step-main" onClick={(e) => this.stepClicked(step, e)}>
                    <div className="editor-step-content">
                        {parent && <ChildStepIcon/>}
                        <div className="editor-step-title">
                            <span className="editor-step-label">{step.label}</span>
                            {!errors && <span className="editor-step-summary">
                                {thisMeta && thisMeta.parameters.filter(p => p.isRequired).map(p =>
                                    <span>{step.data[p.name]} </span>
                                )}
                                </span>
                            }
                            {errors && <span className="editor-step-errors">
                                {errors.map(err =>
                                    <div>{err.error ? err.error : err}</div>
                                )}
                                </span>
                            }
                        </div>
                    </div>

                    {step.isContainer && this.renderSteps(step.children, step)}
                </div>
            </div>
        );
    }

    stepClicked(step:StepInfo, e:HTMLEvent) {
        e.stopPropagation(); // Don't bubble up to parent

        const {onStepSelected} = this.props;

        if (onStepSelected) {
            onStepSelected(step);
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
        if (!this.state.stepMetadata) {
            return null;
        }
        const { steps, parent } = this.props;
        return (<div className="editor-steps">
            {this.renderSteps(steps, parent)}
            <div className="editor-button-bar">
                <button className="btn-primary add" onClick={(e) => this.addStepClicked(e)}>
                    <Icon icon="add" size={20} />
                    Add step
                </button>
            </div>
        </div>);
    }
}
