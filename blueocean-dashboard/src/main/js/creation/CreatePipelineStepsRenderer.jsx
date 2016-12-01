/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import Extensions from '@jenkins-cd/js-extensions';

import FlowStep from './flow2/FlowStep';
import MultiStepFlow from './flow2/MultiStepFlow';
import VerticalStep from './flow2/VerticalStep';

const Sandbox = Extensions.SandboxedComponent;

/**
 * Displays the current steps based on the selection in the SCM provider list.
 */
@observer
export class CreatePipelineStepsRenderer extends React.Component {

    constructor(props) {
        super(props);

        this.flowManager = null;
    }

    shouldComponentUpdate(nextProps) {
        const providerChanged = this.props.selectedProvider !== nextProps.selectedProvider;

        if (providerChanged) {
            console.log('providerChanged?', providerChanged);
            try {
                this.flowManager = nextProps.selectedProvider.getFlowManager();
                this.flowManager.initialize(this);
            } catch (error) {
                console.warn('Error rendering:', this.props.selectedProvider, error);
            }
        }

        return providerChanged;
    }

    stepsChanged() {
        console.log('stepsChanged');
        // this.forceUpdate();
    }

    hasSteps() {
        return this.flowManager && this.flowManager.activeSteps && this.flowManager.activeSteps.length;
    }

    render() {
        if (!this.hasSteps()) {
            return null;
        }

        const props = {
            flowManager: this.flowManager,
        };

        // create Step elements for each "pending" text and
        // then combine with the actual rendered steps
        const pendingSteps = this.flowManager.pendingSteps.map(text => {
            return (
                <FlowStep title={text} />
            );
        });

        const allSteps = [].concat(
            this.flowManager.activeSteps.slice(),
            pendingSteps
        );

        return (
            <Sandbox>
                <MultiStepFlow>
                    {React.Children.map(allSteps, child => {
                        return React.cloneElement(child, props);
                    })}
                </MultiStepFlow>
            </Sandbox>
        );
    }
}

CreatePipelineStepsRenderer.propTypes = {
    selectedProvider: PropTypes.object,
    onCompleteFlow: PropTypes.func,
};
