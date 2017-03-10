// @flow

import React, { Component, PropTypes } from 'react';
import pipelineMetadataService from '../../services/PipelineMetadataService';
import { Dialog } from '@jenkins-cd/design-language';
import { Icon } from "react-material-icons-blue";
import debounce from 'lodash.debounce';

const isStepValidForSelectionUI = (step) => {
    switch (step.type) {
        case 'org.jenkinsci.plugins.workflow.support.steps.StageStep':
        case 'org.jenkinsci.plugins.docker.workflow.WithContainerStep':
            return false;
    }
    return true;
};

// JENKINS-41360
const knownStepOrder = [
    'sh', // Shell Script
    'echo', // Print Message
    'timeout', // Enforce Time Limit
    'retry', // Retry the body...
    'sleep', // Sleep
    'bat', // Windows Bash Script
    'archiveArtifacts', // Archive The artifacts
    'node', // Allocate Node
];

const stepSorter = (a,b) => {
    const ai = knownStepOrder.indexOf(a.functionName);
    const bi = knownStepOrder.indexOf(b.functionName);
    if (ai < 0 && bi < 0) {
        return a.displayName.localeCompare(b.displayName);
    }
    if (ai < 0) {
        return 1;
    }
    if (bi < 0) {
        return -1;
    }
    return ai < bi ? -1 : 1;
};

type Props = {
    onClose?: () => any,
    onStepSelected: (step:StepInfo) => any,
}

type State = {
    selectedStep?: () => any,
    stepMetadata: Array<any>,
    searchFilter: Function,
};

type DefaultProps = typeof AddStepSelectionSheet.defaultProps;

export class AddStepSelectionSheet extends Component<DefaultProps, Props, State> {
    props:Props;
    state:State;

    constructor(props:Props) {
        super(props);
        this.state = { steps: null, selectedStep: null, searchFilter: e => true };
    }

    componentWillMount() {
        pipelineMetadataService.getStepListing(stepMetadata => {
            this.setState({stepMetadata: stepMetadata});
        });
    }

    componentDidMount() {
        this.refs.searchInput.focus();
    }

    closeDialog() {
        this.props.onClose();
    }

    addStep(step) {
        this.props.onStepSelected(step);
        this.closeDialog();
    }

    filterSteps = debounce((value) => {
        const searchTerm = value.toLowerCase();
        this.setState({searchFilter: s => s.displayName.toLowerCase().indexOf(searchTerm) !== -1});
    }, 300);
    
    selectItemByKeyPress(e, step) {
        if (e.key == 'Enter') {
            this.props.onStepSelected(step);
            this.closeDialog();
        }
    }

    render() {
        const { stepMetadata, selectedStep } = this.state;
        
        return (
            <div className="editor-step-selection-dialog">
                <div className="editor-step-search">
                    <Icon icon="search" style={{ fill: '#ddd' }} size={22} />
                    <input ref="searchInput" type="text" className="editor-step-search-input" onChange={e => this.filterSteps(e.target.value)}
                        placeholder="Find steps by name" />
                </div>
                <div className="editor-step-selector">
                {stepMetadata && stepMetadata.filter(isStepValidForSelectionUI).filter(this.state.searchFilter).sort(stepSorter).map(step =>
                    <div tabIndex="0" onKeyPress={e => this.selectItemByKeyPress(e, step)}
                        onClick={() => this.addStep(step)}>
                        {step.displayName}
                    </div>
                )}
                </div>
            </div>
        );
    }
}
