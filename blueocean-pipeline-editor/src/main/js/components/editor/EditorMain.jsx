// @flow

import React, { Component } from 'react';
import { EditorPipelineGraph } from './EditorPipelineGraph';
import { EditorStepList } from './EditorStepList';
import { EditorStepDetails } from './EditorStepDetails';
import { AgentConfiguration } from './AgentConfiguration';
import { EnvironmentConfiguration } from './EnvironmentConfiguration';
import { AddStepSelectionSheet } from './AddStepSelectionSheet';
import pipelineStore from '../../services/PipelineStore';
import type { StageInfo, StepInfo } from '../../services/PipelineStore';
import pipelineMetadataService from '../../services/PipelineMetadataService';
import { Sheets } from '../Sheets';
import { Accordion } from '../Accordion';
import { MoreMenu } from '../MoreMenu';
import pipelineValidator from '../../services/PipelineValidator';
import { ValidationMessageList } from './ValidationMessageList';
import focusOnElement from './focusOnElement';
import debounce from 'lodash.debounce';
import { Dialog, TextInput, FormElement } from '@jenkins-cd/design-language';

type Props = {
};

type State = {
    selectedStage: ?StageInfo,
    selectedSteps: StepInfo[],
    showSelectStep: ?boolean,
    parentStep: ?StepInfo,
    stepMetadata: ?Object,
    dialog: any,
};

type DefaultProps = typeof EditorMain.defaultProps;

function ConfigPanel({className, children}) {
    return (<div className={className}>
        {children}
    </div>);
}

function cleanPristine(node, visited = []) {
    if (visited.indexOf(node) >= 0) {
        return;
    }
    visited.push(node);
    if (node.pristine) {
        delete node.pristine;
    }
    for (const key of Object.keys(node)) {
        const val = node[key];
        if (val instanceof Object) {
            cleanPristine(val, visited);
        }
    }
}

function _getStageErrors(stage, ...excludeProps) {
    if (!stage) {
        return null;
    }
    // return pipelineValidator.getNodeValidationErrors(stage);
    const excludedNodes = [];
    for (const prop of excludeProps) {
        excludedNodes.push(stage[prop]);
    }
    return pipelineValidator.getAllValidationErrors(stage, excludedNodes);
}

function _findParentComponent(component) {
    return component._reactInternalInstance._currentElement._owner._instance;
}

function _findReactComponent(domElement) {
    if (domElement) {
        for (var key in domElement) {
            if (key.startsWith("__reactInternalInstance$")) {
                var compInternals = domElement[key]._currentElement;
                if (compInternals) {
                    var compWrapper = compInternals._owner;
                    if (compWrapper) {
                        var comp = compWrapper._instance;
                        if (comp) {
                            return comp;
                        }
                    }
                }
            }
        }
        if (domElement != document && domElement.parentElement) {
            return _findReactComponent(domElement.parentElement);
        }
    }
    return null;
}

export class EditorMain extends Component<DefaultProps, Props, State> {

    static defaultProps = {};

    //static propTypes = {...}
    // TODO: React proptypes ^^^

    props:Props;
    state:State;
    pipelineUpdated: Function;

    constructor() {
        super();
        this.state = { selectedSteps: [] };
    }

    componentWillMount() {
        pipelineStore.addListener(this.pipelineUpdated = p => this.doUpdate());
        pipelineMetadataService.getStepListing(stepMetadata => {
            this.setState({stepMetadata: stepMetadata});
        });
    }

    componentDidMount() {
        document.addEventListener('keydown', this.validateAfterTyping = debounce(e => {
            const component = _findReactComponent(e.target);
            if (!e.target.classList.contains('stage-name-edit')) {
                pipelineValidator.validate();
            }
        }, 1000));
    }

    componentWillUnmount() {
        pipelineStore.removeListener(this.pipelineUpdated);
        document.removeEventListener('keydown', this.validateAfterTyping);
    }

    doUpdate() {
        for (const step of this.state.selectedSteps) {
            if (!pipelineStore.findStageByStep(step)) {
                this.setState({selectedSteps: []});
            }
        }
        if (this.state.selectedStage && !pipelineStore.findParentStage(this.state.selectedStage)) {
            this.setState({selectedStage: null});
        } else {
            this.forceUpdate();
        }
    }

    promptForParallelName(parentStage:StageInfo) {
        let val = '';
        const createStage = () => {
            this.setState({ dialog: null }, () => this.createStage(parentStage, val));
        };
        this.setState({ dialog: (
            <Dialog className="name-new-parallel" onDismiss={() => this.setState({dialog: null})}
                    title="Name Parallel Group"
                    buttons={<div><button onClick={() => createStage()}>Create</button></div>}>
                <FormElement title="Group Name">
                    <TextInput isRequired
                               defaultValue={val}
                               onChange={v => val = v}
                               onEnterPressed={() => createStage()}/>
                </FormElement>
            </Dialog>
        )}, () => document.querySelector('.name-new-parallel input').focus());
    }

    createStage(parentStage:StageInfo, parallelGroupName:string) {
        if (parentStage && !parallelGroupName) {
            if (!parentStage.children || !parentStage.children.length) {
                this.promptForParallelName(parentStage);
                return;
            }
        }
        const newStage = parentStage
            ? pipelineStore.createParallelStage('', parentStage)
            : pipelineStore.createSequentialStage('');

        if (parallelGroupName) {
            parentStage.name = parallelGroupName;
        }

        this.setState({
            selectedStage: newStage,
            selectedSteps: [],
        }, e => {
            setTimeout(() => {
                document.querySelector('.stage-name-edit').focus();
            }, 200);
        });
    }

    graphSelectedStageChanged(newSelectedStage:?StageInfo) {
        this.setState({
            selectedStage: newSelectedStage,
            selectedSteps: [],
            showSelectStep: false,
        });
        pipelineValidator.validate();
    }

    openSelectStepDialog(parentStep: ?StepInfo = null) {
        this.setState({showSelectStep: true, parentStep: parentStep});
    }

    selectedStepChanged(step: StepInfo, parentStep: ?StepInfo) {
        let { selectedSteps } = this.state;
        if (!step) {
            selectedSteps.pop();
        } else {
            if (parentStep) {
                selectedSteps.push(step);
            } else {
                selectedSteps = [ step ];
            }
        }
        this.setState({selectedSteps, showSelectStep: false});
    }

    stepDataChanged(newStep:any) {
        if (newStep.pristine) {
            return;
        }
        this.forceUpdate();
        pipelineValidator.validate();
    }

    addStep(step: any) {
        const { selectedSteps } = this.state;
        const newStep = pipelineStore.addStep(this.state.selectedStage, this.state.parentStep, step);
        newStep.pristine = true;
        selectedSteps.push(newStep);
        this.setState({showSelectStep: false, selectedSteps}, e => focusOnElement('.sheet:last-child .editor-step-detail input,.sheet:last-child .editor-step-detail textarea'));
        pipelineValidator.validate();
    }

    deleteStep(step: any) {
        const { selectedSteps } = this.state;
        pipelineStore.deleteStep(step);
        selectedSteps.pop(); // FIXME
        this.setState({selectedSteps});
        pipelineValidator.validate();
    }

    deleteStageClicked(e:HTMLEvent) {
        e.target.blur(); // Don't leave ugly selection highlight

        const {selectedStage} = this.state;

        if (selectedStage) {
            pipelineStore.deleteStage(selectedStage);
        }
        pipelineValidator.validate();
    }

    render() {
        const { selectedStage, selectedSteps, stepMetadata } = this.state;

        if (!stepMetadata) {
            return null;
        }

        const sheets = [];
        const steps = selectedStage ? selectedStage.steps : [];
        const hasChildStages = selectedStage && selectedStage.children && selectedStage.children.length;
        const title = selectedStage ? selectedStage.name : 'Select or create a pipeline stage';

        // Global config panel
        if (pipelineStore.pipeline) {
            sheets.push(
                <ConfigPanel className="editor-config-panel global"
                          key={'globalConfig'+pipelineStore.pipeline.id}
                          title={<h4>
                              Pipeline Settings
                          </h4>}>
                    <div className="editor-stage-settings" key="settings">
                        <AgentConfiguration key={'agent'+pipelineStore.pipeline.id} node={pipelineStore.pipeline} onChange={agent => (selectedStage && agent.type == 'none' ? delete pipelineStore.pipeline.agent : pipelineStore.pipeline.agent = agent) && this.pipelineUpdated()} />
                        <EnvironmentConfiguration key={'env'+pipelineStore.pipeline.id} node={pipelineStore.pipeline} onChange={e => this.pipelineUpdated()} />
                    </div>
                </ConfigPanel>
            );
        }

        // Stage config panel
        if (selectedStage) {
            // Determine if we need to show a particular configuration page
            // and what errors to display
            const sectionErrors = {};
            sectionErrors.stage = _getStageErrors(selectedStage, 'children', 'steps', 'agent', 'environment', 'when', 'post');
            sectionErrors.steps = _getStageErrors(selectedStage.steps);
            sectionErrors.settings = _getStageErrors(selectedStage.agent) || _getStageErrors(selectedStage.environment);
            sectionErrors.show = sectionErrors.steps ? 'steps' : (sectionErrors.settings ? 'settings' : null);

            sheets.push(
                <ConfigPanel className="editor-config-panel stage" key={'stageConfig' + selectedStage.id}
                             onClose={e => cleanPristine(selectedStage) || pipelineValidator.validate() || this.graphSelectedStageChanged(null)}
                             title={
                                 <div>
                                     <input className="stage-name-edit" placeholder="Name your stage"
                                            defaultValue={title}
                                            onChange={e => (selectedStage.name = e.target.value) && this.pipelineUpdated()}/>
                                     <MoreMenu>
                                         <a onClick={e => this.deleteStageClicked(e)}>Delete</a>
                                     </MoreMenu>
                                     <ValidationMessageList errors={sectionErrors.stage}/>
                                 </div>
                             }>
                    <Accordion show={sectionErrors.show} key={'stageSections' + selectedStage.id}>
                        {!hasChildStages &&
                        <div title="Steps" key="steps">
                            <EditorStepList steps={steps}
                                            onAddStepClick={() => this.openSelectStepDialog()}
                                            onAddChildStepClick={parent => this.openSelectStepDialog(parent)}
                                            onStepSelected={(step) => this.selectedStepChanged(step)}/>
                        </div>
                        }
                        <div title="Settings" className="editor-stage-settings" key="settings">
                            {!hasChildStages &&
                            <AgentConfiguration node={selectedStage}
                                                onChange={agent => (selectedStage && agent.type == 'none' ? delete selectedStage.agent : selectedStage.agent = agent) && this.pipelineUpdated()}/>
                            }
                            <EnvironmentConfiguration node={selectedStage} onChange={e => this.pipelineUpdated()}/>
                        </div>
                    </Accordion>
                </ConfigPanel>
            );
        }

        let parentStep = null;
        for (const step of selectedSteps) {
            const stepConfigPanel = (<EditorStepDetails className="editor-config-panel step"
                    step={step} key={step.id}
                    onDataChange={newValue => this.stepDataChanged(newValue)}
                    onClose={e => cleanPristine(step) || pipelineValidator.validate() || this.selectedStepChanged(null, parentStep)}
                    openSelectStepDialog={step => this.openSelectStepDialog(step)}
                    selectedStepChanged={step => this.selectedStepChanged(step, parentStep)}
                    title={<h4>
                        {selectedStage && selectedStage.name} / {step.label}
                        <MoreMenu>
                            <a onClick={e => this.deleteStep(step)}>Delete</a>
                        </MoreMenu>
                    </h4>} />);

            if (stepConfigPanel) sheets.push(stepConfigPanel);
            parentStep = step;
        }

        const stepAddPanel = this.state.showSelectStep && (<AddStepSelectionSheet
                onClose={() => this.setState({showSelectStep: false})}
                onStepSelected={step => this.addStep(step)}
                title={<h4>Choose step type</h4>} />);

        if (stepAddPanel) sheets.push(stepAddPanel);

        return (
            <div className="editor-main" key={pipelineStore.pipeline && pipelineStore.pipeline.id}>
                <div className="editor-main-graph" onClick={e => cleanPristine(pipelineStore.pipeline) || pipelineValidator.validate() || this.setState({selectedStage: null, selectedSteps: []})}>
                    <ValidationMessageList errors={_getStageErrors(pipelineStore.pipeline, 'children')} />
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
                {this.state.dialog}
            </div>
        );
    }
}
