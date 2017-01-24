// @flow

import React, { Component, PropTypes } from 'react';
import { EditorPipelineGraph } from './EditorPipelineGraph';
import { EditorStepList } from './EditorStepList';
import { EditorStepDetails } from './EditorStepDetails';
import { AgentConfiguration } from './AgentConfiguration';
import { EnvironmentConfiguration } from './EnvironmentConfiguration';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { AddStepSelectionSheet } from './AddStepSelectionSheet';
import pipelineStore from '../../services/PipelineStore';
import type { StageInfo, StepInfo } from '../../services/PipelineStore';
import { Sheets } from '../Sheets';
import { MoreMenu } from '../MoreMenu';
import { Icon } from "@jenkins-cd/react-material-icons";

type Props = {
};

type State = {
    selectedStage: ?StageInfo,
    selectedStep: ?StepInfo,
    showSelectStep: ?boolean,
    parentStep: ?StepInfo,
};

type DefaultProps = typeof EditorMain.defaultProps;
export class EditorMain extends Component<DefaultProps, Props, State> {

    static defaultProps = {};

    //static propTypes = {...}
    // TODO: React proptypes ^^^

    props:Props;
    state:State;
    pipelineUpdated: Function;

    componentWillMount() {
        this.handleProps(
            this.props,
            this.props,
            this.state);
        pipelineStore.addListener(this.pipelineUpdated = p => this.doUpdate());
    }

    componentWillUnmount() {
        pipelineStore.removeListener(this.pipelineUpdated);
    }

    doUpdate() {
        if (this.state.selectedStage && !pipelineStore.findParentStage(this.state.selectedStage)) {
            this.setState({selectedStage: null});
        } else {
            this.forceUpdate();
        }
    }

    componentWillReceiveProps(nextProps:Props) {
        //this.handleProps(nextProps, this.props, this.state);
    }

    createStage(parentStage:StageInfo) {
        const newStage = parentStage
            ? pipelineStore.createParallelStage('', parentStage)
            : pipelineStore.createSequentialStage('');
        this.setState({
            selectedStage: newStage,
            selectedStep: null
        }, e => {
            setTimeout(() => {
                document.querySelector('.stage-name-edit').focus();
            }, 200);
        });
    }
    
    handleProps(nextProps:Props, oldProps:Props, state:State) {

        let updates = {},
            stagesChanged = false,
            stageStepsChanged = false;

        // Update stages list?
        if (nextProps.pipeline && nextProps.pipeline.children !== oldProps.pipeline.children) {
            let stages = nextProps.pipeline.children;
            updates.selectedStage = (stages && stages[0]) || null;
            stagesChanged = true;
        }

        // If we've changed either stages or steps, we need a new selectedStep
        if (stagesChanged || stageStepsChanged) {
            let selectedStep = null; // If we don't find a first step we'll clear any old value

            let selectedStage = stagesChanged ? updates.selectedStage : state.selectedStage;

            if (selectedStage) {
                let stepsForStage = selectedStage.steps;
                if (stepsForStage && stepsForStage.length) {
                    selectedStep = stepsForStage[0];
                }
            }

            updates.selectedStep = selectedStep;
        }

        this.setState(updates);
    }

    graphSelectedStageChanged(newSelectedStage:?StageInfo) {
        this.setState({
            selectedStage: newSelectedStage,
            selectedStep: null,
            showSelectStep: false,
        });
    }

    openSelectStepDialog(parentStep: ?StepInfo = null) {
        this.setState({showSelectStep: true, parentStep: parentStep});
    }

    selectedStepChanged(selectedStep:StepInfo) {
        this.setState({selectedStep, showSelectStep: false});
    }

    stepDataChanged(newStep:any) {
        const {selectedStage,selectedStep} = this.state;

        if (!selectedStep) {
            console.log("unable to set new step data, no currently selected step");
            return;
        }

        const parentStep = pipelineStore.findParentStep(selectedStep);
        const stepArray = (parentStep && parentStep.children) || selectedStage.steps;
        let idx = 0;
        for (; idx < stepArray.length; idx++) {
            if (stepArray[idx].id === selectedStep.id) {
                break;
            }
        }
        stepArray[idx] = newStep;
        this.setState({
            selectedStep: newStep
        });
    }

    addStep(step: any) {
        const newStep = pipelineStore.addStep(this.state.selectedStage, this.state.parentStep, step);
        this.setState({showSelectStep: false, selectedStep: newStep}, e => {
            setTimeout(() => {
                document.querySelector('.editor-step-detail input,.editor-step-detail textarea').focus();
            }, 200);
        });
    }

    deleteStep(step: any) {
        pipelineStore.deleteStep(step);
        this.setState({selectedStep: null});
    }

    deleteStageClicked(e:HTMLEvent) {
        e.target.blur(); // Don't leave ugly selection highlight

        const {selectedStage} = this.state;

        if (selectedStage) {
            pipelineStore.deleteStage(selectedStage);
        }
    }

    render() {
        const {selectedStage, selectedStep} = this.state;
        const steps = selectedStage ? selectedStage.steps : [];

        const title = selectedStage ? selectedStage.name : 'Select or create a pipeline stage';
        const disableIfNoSelection = selectedStage ? {} : {disabled: 'disabled'}; // TODO: Delete if we don't use this any more

        // FIXME - agents are defined at the top stage level, this will change
        let configurationStage = selectedStage && (pipelineStore.findParentStage(selectedStage) || selectedStage);
        if (pipelineStore.pipeline === configurationStage) {
            configurationStage = selectedStage;
        }

        const globalConfigPanel = pipelineStore.pipeline && (<div className="editor-config-panel global"
            key={'globalConfig'+pipelineStore.pipeline.id}
            title={<h4>
                    Pipeline Settings
                </h4>}>
            <AgentConfiguration key={'agent'+pipelineStore.pipeline.id} node={pipelineStore.pipeline} onChange={agent => (selectedStage && agent.type == 'none' ? delete pipelineStore.pipeline.agent : pipelineStore.pipeline.agent = agent) && this.pipelineUpdated()} />
            <EnvironmentConfiguration key={'env'+pipelineStore.pipeline.id} node={pipelineStore.pipeline} onChange={e => this.pipelineUpdated()} />
        </div>);

        const stageConfigPanel = selectedStage && (<div className="editor-config-panel stage" key={'stageConfig'+selectedStage.id}
            onClose={e => this.graphSelectedStageChanged(null)}
            title={
                <div>
                    <input className="stage-name-edit" placeholder="Name your stage" defaultValue={title} 
                        onChange={e => (selectedStage.name = e.target.value) && this.pipelineUpdated()} />
                    <MoreMenu>
                        <a onClick={e => this.deleteStageClicked(e)}>Delete</a>
                    </MoreMenu>
                </div>
            }>
            <EditorStepList steps={steps}
                        selectedStep={selectedStep}
                        onAddStepClick={() => this.openSelectStepDialog()}
                        onAddChildStepClick={parent => this.openSelectStepDialog(parent)}
                        onStepSelected={(step) => this.selectedStepChanged(step)} />
            {/*
            <AgentConfiguration key={'agent'+configurationStage.id} node={configurationStage} onChange={agent => (selectedStage && agent.type == 'none' ? delete configurationStage.agent : configurationStage.agent = agent) && this.pipelineUpdated()} />
            <EnvironmentConfiguration key={'env'+configurationStage.id} node={configurationStage} onChange={e => this.pipelineUpdated()} />
            */}
        </div>);

        const stepConfigPanel = selectedStep && (<EditorStepDetails className="editor-config-panel step"
                step={selectedStep} key={steps.indexOf(selectedStep)}
                onDataChange={newValue => this.stepDataChanged(newValue)}
                onClose={e => this.selectedStepChanged(null)}
                title={<h4>
                    {selectedStage.name} / {selectedStep.label}
                    <MoreMenu>
                        <a onClick={e => this.deleteStep(selectedStep)}>Delete</a>
                    </MoreMenu>
                </h4>} />);

        const stepAddPanel = this.state.showSelectStep && (<AddStepSelectionSheet
                onClose={() => this.setState({showSelectStep: false})}
                onStepSelected={step => this.addStep(step)}
                title={<h4>Choose step type</h4>} />);

        const sheets = [];
        if (globalConfigPanel) sheets.push(globalConfigPanel);
        if (stageConfigPanel) sheets.push(stageConfigPanel);
        if (stepConfigPanel) sheets.push(stepConfigPanel);
        if (stepAddPanel) sheets.push(stepAddPanel);

        return (
            <div className="editor-main" key={pipelineStore.pipeline && pipelineStore.pipeline.id}>
                <div className="editor-main-graph" onClick={e => this.setState({selectedStage: null, selectedStep: null})}>
                    {pipelineStore.pipeline &&
                    <EditorPipelineGraph stages={pipelineStore.pipeline.children}
                                         selectedStage={selectedStage}
                                         onStageSelected={(stage) => this.graphSelectedStageChanged(stage)}
                                         onCreateStage={(parentStage) => this.createStage(parentStage)}/>
                    }
                </div>
                <Sheets>
                {sheets}
                </Sheets>
            </div>
        );
    }
}
