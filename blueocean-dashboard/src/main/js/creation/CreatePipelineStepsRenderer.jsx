/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import Extensions from '@jenkins-cd/js-extensions';
import VerticalStep from './VerticalStep';

const Sandbox = Extensions.SandboxedComponent;

export class CreatePipelineStepsRenderer extends React.Component {

    shouldComponentUpdate(nextProps) {
        return this.props.selectedProvider !== nextProps.selectedProvider;
    }

    render() {
        if (!this.props.selectedProvider) {
            return (
                <VerticalStep className="last-step">
                    <h1>Completed</h1>
                </VerticalStep>
            );
        }

        const props = {
            onCompleteFlow: this.props.onCompleteFlow,
        };

        let defaultFlow;

        try {
            defaultFlow = this.props.selectedProvider.getDefaultFlow();
        } catch (error) {
            console.warn('Error rendering:', this.props.selectedProvider, error);
            return Extensions.ErrorUtils.errorToElement(error);
        }

        return (
            <Sandbox>
                {React.cloneElement(defaultFlow, props)}
            </Sandbox>
        );
    }
}

CreatePipelineStepsRenderer.propTypes = {
    selectedProvider: PropTypes.object,
    onCompleteFlow: PropTypes.func,
};
