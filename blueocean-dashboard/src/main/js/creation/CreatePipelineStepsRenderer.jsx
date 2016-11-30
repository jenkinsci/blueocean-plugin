/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import Extensions from '@jenkins-cd/js-extensions';
import VerticalStep from './flow2/VerticalStep';
import MultiStepFlow from './flow2/MultiStepFlow';

const Sandbox = Extensions.SandboxedComponent;

/**
 * Displays the current steps based on the selection in the SCM provider list.
 */
export class CreatePipelineStepsRenderer extends React.Component {

    flowManager: null;

    constructor(props) {
        super(props);

        this.state = {
            steps: [],
        };
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
        this.forceUpdate();
    }

    hasSteps() {
        return this.flowManager && this.flowManager.activeSteps && this.flowManager.activeSteps.length;
    }

    render() {
        if (!this.hasSteps()) {
            return (
                <VerticalStep className="last-step">
                    <h1>Completed</h1>
                </VerticalStep>
            );
        }

        const props = {
            flowManager: this.flowManager,
        };


        return (
            <Sandbox>
                <MultiStepFlow>
                    {React.Children.map(this.flowManager.activeSteps, child => {
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
