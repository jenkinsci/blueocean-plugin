// @flow

import React, { Component, PropTypes } from 'react';
import pipelineStepListStore from '../../services/PipelineStepListStore';
import { Dialog } from '@jenkins-cd/design-language';
import { Icon } from "react-material-icons-blue";

type Props = {
    onClose?: () => any,
    onStepSelected?: (step:StepInfo) => any,
}

type State = {
    selectedStep?: () => any,
    steps: Array<any>,
    searchFilter: Function,
};

type DefaultProps = typeof AddStepSelectionDialog.defaultProps;

export class AddStepSelectionDialog extends Component<DefaultProps, Props, State> {
    props:Props;
    state:State;

    constructor(props:Props) {
        super(props);
        this.state = { steps: null, selectedStep: null, searchFilter: e => true };
    }
    
    componentWillMount() {
        pipelineStepListStore.getStepListing(data => {
            this.setState({steps: data});
        });
    }
    
    closeDialog() {
        this.props.onClose();
    }
    
    selectAddStep() {
        this.props.onStepSelected(this.state.selectedStep);
        this.closeDialog();
    }
    
    filterSteps(e) {
        const searchTerm = e.target.value.toLowerCase();
        this.setState({searchFilter: s => s.displayName.toLowerCase().indexOf(searchTerm) !== -1});
    }

    render() {
        const { steps, selectedStep } = this.state;
        
        if (!steps) {
            return null;
        }
        
        const buttons = [
            <button className="btn-secondary" onClick={() => this.closeDialog()}>Cancel</button>,
            <button disabled={!this.state.selectedStep} onClick={() => this.selectAddStep()}>Use step</button>,
        ];
        
        return (
            <Dialog onDismiss={() => this.closeDialog()} title="Add Step" buttons={buttons}>
                <div className="editor-step-selection-dialog">
                    <div className="editor-step-selection-dialog-search">
                        <Icon icon="search" style={{ fill: '#ddd' }} size={32} />
                        <input type="text" className="editor-step-selection-dialog-search-input" onChange={e => this.filterSteps(e)}
                            placeholder="Find steps by name" />
                    </div>
                    <div className="editor-step-selection-dialog-steps">
                    {steps.filter(this.state.searchFilter).map(s =>
                        <div tabIndex="0" onKeyPress={() => this.setState({selectedStep: s})} onClick={() => this.setState({selectedStep: s})} className={'step-item' + (this.state.selectedStep === s ? ' selected' : '')}>
                            {s.displayName}
                        </div>
                    )}
                    </div>
                </div>
            </Dialog>
        );
    }
}
