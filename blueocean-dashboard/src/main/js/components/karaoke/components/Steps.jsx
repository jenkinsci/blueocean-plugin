import React, { Component, PropTypes } from 'react';
import { QueuedState } from './QueuedState';
import { Step } from './Step';

export default class Steps extends Component {
    render() {
        const { t, nodeInformation } = this.props;
        // Early out
        if (!nodeInformation) {
            const queuedMessage = t('rundetail.pipeline.pending.message', { defaultValue: 'Waiting for backend to response' });
            return <QueuedState message={queuedMessage} />;
        }
        const { model } = nodeInformation;
        return <div className="Steps">{model.map(item => <Step key={item.key} step={item} {...this.props} />)}</div>;
    }
}

Steps.propTypes = {
    nodeInformation: PropTypes.object.isRequired,
    t: PropTypes.func,
    onUserExpand: PropTypes.func,
    onUserCollapse: PropTypes.func,
    tailLogs: PropTypes.bool,
};
