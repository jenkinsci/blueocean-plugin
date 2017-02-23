import React, { PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { Icon } from '@jenkins-cd/react-material-icons';

export const QueuedState = ({ className = 'waiting', message = 'Waiting for run to start.' }) => (
    <EmptyStateView tightSpacing>
        <p>
            <Icon {...{
                size: 20,
                icon: 'timer',
                style: { fill: '#fff' },
            }}
            />
            <span className={className}>&nbsp;{message}</span>
        </p>
    </EmptyStateView>
);

QueuedState.propTypes = {
    className: PropTypes.string,
    message: PropTypes.object,
};

export const NoSteps = ({ message = 'There are no logs.' }) => (<EmptyStateView tightSpacing>
    <p>{message}</p>
</EmptyStateView>);

NoSteps.propTypes = {
    message: PropTypes.object,
};
