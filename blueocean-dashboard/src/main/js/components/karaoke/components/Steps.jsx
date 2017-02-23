import React, { Component, PropTypes } from 'react';
import { QueuedState, NoSteps } from './QueuedState';
import { Step } from './Step';

export default class Steps extends Component {
    render() {
        const { t, nodeInformation } = this.props;
        // Early out
        if (!nodeInformation) {
            const queuedMessage = t('rundetail.pipeline.pending.message',
                { defaultValue: 'Waiting for backend to response' });
            return <QueuedState message={queuedMessage} />;
        }
        const { model } = nodeInformation;
        if (model.length === 0) {
            return (<NoSteps message={t('rundetail.pipeline.nosteps',
                { defaultValue: 'There are no logs' })}
            />);
        }
        // const stepRenderer = model.map((item, index) => <div>{index}</div>);
        const stepRenderer = model.map((item, index) => <Step key={index} { ...{ ...this.props, step: item } } />);
        return (<div>{ stepRenderer }</div>);
    }
}

Steps.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
    t: PropTypes.func,
};

