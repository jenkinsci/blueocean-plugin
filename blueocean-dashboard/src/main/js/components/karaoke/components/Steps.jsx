import React, { Component, PropTypes } from 'react';
import { QueuedState } from './QueuedState';
import { Step } from './Step';

export default class Steps extends Component {
    render() {
        const { t, nodeInformation, followAlong } = this.props;
        // Early out
        if (!nodeInformation) {
            const queuedMessage = t('rundetail.pipeline.pending.message',
                { defaultValue: 'Waiting for backend to response' });
            return <QueuedState message={queuedMessage} />;
        }

        // console.log('re-rendering Steps.jsx', followAlong);

        const { model } = nodeInformation;
        return (<div className="Steps">
            { model.map((step, idx) => <Step
                { ...
                    { ...this.props,
                        key: step.id,
                        step,
                        isFocused: followAlong && step.isRunning,
                        scrollToBottom: followAlong && idx === model.length - 1,
                    }
                }
            />) }
        </div>);
    }
}

Steps.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
    t: PropTypes.func,
    followAlong: PropTypes.bool,
    // scrollToNode: PropTypes.object,
};

