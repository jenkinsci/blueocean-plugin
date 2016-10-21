/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import Extensions from '@jenkins-cd/js-extensions';
import VerticalStep from './VerticalStep';

export class CreatePipelineStepsRenderer extends React.Component {

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

        return React.cloneElement(this.props.selectedProvider.getDefaultFlow(), props);
    }
}

CreatePipelineStepsRenderer.propTypes = {
    selectedProvider: PropTypes.object,
    onCompleteFlow: PropTypes.func,
};
