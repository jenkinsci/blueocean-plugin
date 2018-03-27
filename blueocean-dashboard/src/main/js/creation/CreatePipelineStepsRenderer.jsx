/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from './flow2/FlowStep';
import MultiStepFlow from './flow2/MultiStepFlow';

/**
 * Displays the current steps based on the selection in the SCM provider list.
 */
@observer
export class CreatePipelineStepsRenderer extends React.Component {
    constructor(props) {
        super(props);

        this.flowManager = null;

        this._initializeFlow(props.selectedProvider);
    }

    componentWillReceiveProps(nextProps) {
        this._initializeFlow(nextProps.selectedProvider);
    }

    onComplete(payload) {
        if (this.props.onCompleteFlow) {
            this.props.onCompleteFlow(payload);
        }
    }

    _initializeFlow(provider) {
        if (!provider) return;

        try {
            this.flowManager = provider.getFlowManager();
            this.flowManager.initialize(this);
        } catch (error) {
            console.warn('Error rendering:', provider, error);
        }
    }

    hasSteps() {
        return this.flowManager && this.flowManager.steps && this.flowManager.steps.length;
    }

    render() {
        if (!this.hasSteps()) {
            return null;
        }

        const props = {
            flowManager: this.flowManager,
        };

        const steps = this.flowManager.steps.map(step => step.stepElement);

        // create Step elements for each "placeholder" text and
        // then combine with the actual rendered steps
        const placeholderSteps = this.flowManager.placeholders.map(text => <FlowStep title={text} />);

        const allSteps = [].concat(steps, placeholderSteps);

        return (
            <MultiStepFlow className="creation-steps" activeIndex={this.flowManager.activeIndex}>
                {React.Children.map(allSteps, child => React.cloneElement(child, props))}
            </MultiStepFlow>
        );
    }
}

CreatePipelineStepsRenderer.propTypes = {
    selectedProvider: PropTypes.object,
    onCompleteFlow: PropTypes.func,
};
