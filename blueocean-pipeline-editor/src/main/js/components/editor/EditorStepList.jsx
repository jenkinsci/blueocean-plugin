// @flow

import React, { Component, PropTypes } from 'react';
import { DragDropContext } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';

import pipelineMetadataService from '../../services/PipelineMetadataService';
import { EditorStepItem } from './EditorStepItem';
import { EditorStepListDropZone } from './EditorStepListDropZone';
import type { StageInfo, StepInfo } from '../../services/PipelineStore';
import { Icon } from '@jenkins-cd/design-language';
import pipelineValidator from '../../services/PipelineValidator';
import { DragPosition } from './DragPosition';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-pipeline-editor');

type Props = {
    stage: ?StageInfo,
    steps: Array<StepInfo>,
    parent: ?StepInfo,
    onAddStepClick?: () => any,
    onStepSelected?: (step: StepInfo) => any,
    onAddChildStepClick?: (step: StepInfo) => any,
    onDragStepBegin?: () => any,
    onDragStepHover?: () => any,
    onDragStepDrop?: () => any,
    onDragStepEnd?: () => any,
};

type State = {
    stepMetadata: any,
};

type DefaultProps = typeof EditorStepList.defaultProps;

@DragDropContext(HTML5Backend)
export class EditorStepList extends Component<DefaultProps, Props, State> {
    static defaultProps = {
        steps: [],
    };

    //static propTypes = {...}
    // TODO: React proptypes ^^^

    props: Props;
    state: State;

    constructor(props: Props) {
        super(props);
        this.state = {
            stepMetadata: null,
        };
    }

    componentWillMount() {
        pipelineMetadataService.getStepListing(stepMetadata => {
            this.setState({ stepMetadata: stepMetadata });
        });
    }

    renderSteps(steps: StepInfo[], parent: ?StepInfo) {
        if (!steps.length) {
            if (parent) {
                // if no children, render a placeholder to show this is a container
                return (
                    <div className="editor-step nested missing">
                        <div className="editor-step-main" onClick={e => this.stepClicked(parent, e)}>
                            <div className="editor-step-content">
                                <div className="editor-step-title">
                                    <span className="editor-step-summary">
                                        {t('editor.page.common.pipeline.steps.child.require', { default: 'There are no child steps defined' })}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                );
            } else {
                // we know there will be a validation error in this case, show
                // something indicating the user needs a step in a stage
                return (
                    <div className="editor-step missing">
                        <div className="editor-step-main">
                            <div className="editor-step-content">
                                <div className="editor-step-title">
                                    <span className="editor-step-summary">
                                        {t('editor.page.common.pipeline.steps.required', { default: 'There are no steps, at least one is required.' })}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                );
            }
        }

        return steps.map(step => this.renderStep(step, parent));
    }

    renderStep(step: StepInfo, parent: StepInfo) {
        const thisMeta = this.state.stepMetadata.find(step) || {};
        const classNames = ['editor-step'];
        const errors = pipelineValidator.getNodeValidationErrors(step);
        const key = 's_' + step.id;
        if (parent) classNames.push('nested');
        if (errors) classNames.push('errors');
        if (step.isContainer) classNames.push('is-container');

        return (
            <div className={classNames.join(' ')} key={key}>
                <div className="editor-step-main" onClick={e => this.stepClicked(step, e)}>
                    <EditorStepItem
                        stage={this.props.stage}
                        step={step}
                        parameters={thisMeta.parameters}
                        errors={errors}
                        onDragStepBegin={this.props.onDragStepBegin}
                        onDragStepHover={this.props.onDragStepHover}
                        onDragStepDrop={this.props.onDragStepDrop}
                        onDragStepEnd={this.props.onDragStepEnd}
                    />

                    {step.isContainer && [
                        this.renderSteps(step.children, step),
                        <EditorStepListDropZone
                            stage={this.props.stage}
                            step={step}
                            position={DragPosition.LAST_CHILD}
                            onDragStepHover={this.props.onDragStepHover}
                            onDragStepDrop={this.props.onDragStepDrop}
                        />,
                    ]}
                </div>
            </div>
        );
    }

    stepClicked(step: StepInfo, e: HTMLEvent) {
        e.stopPropagation(); // Don't bubble up to parent

        const { onStepSelected } = this.props;

        if (onStepSelected) {
            onStepSelected(step);
        }
    }

    addStepClicked(e: HTMLEvent) {
        e.target.blur(); // Don't leave the button focused
        const { onAddStepClick } = this.props;
        if (onAddStepClick) {
            onAddStepClick();
        }
    }

    addChildStepClicked(parent: StepInfo, e: HTMLEvent) {
        e.stopPropagation(); // Don't bubble up to parent
        e.target.blur(); // Don't leave the button focused
        const { onAddChildStepClick } = this.props;
        if (onAddChildStepClick) {
            onAddChildStepClick(parent);
        }
    }

    render() {
        if (!this.state.stepMetadata) {
            return null;
        }
        const { steps, parent } = this.props;
        return (
            <div className="editor-steps">
                {this.renderSteps(steps, parent)}
                <EditorStepListDropZone
                    stage={this.props.stage}
                    step={this.props.stage}
                    position={DragPosition.LAST_CHILD}
                    onDragStepHover={this.props.onDragStepHover}
                    onDragStepDrop={this.props.onDragStepDrop}
                />
                <div className="editor-button-bar">
                    <button className="btn-primary add" onClick={e => this.addStepClicked(e)}>
                        <Icon icon="ContentAdd" size={20} />
                        {t('editor.page.common.pipeline.steps.add', { default: 'Add step' })}
                    </button>
                </div>
            </div>
        );
    }
}
