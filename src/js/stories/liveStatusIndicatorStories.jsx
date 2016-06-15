import React from 'react';
import { storiesOf } from '@kadira/storybook';
import moment from 'moment';

import { LiveStatusIndicator } from '../components';

storiesOf('LiveStatusIndicator', module)
    .add('10s, start now', scenario1)
    .add('60s, started 30s ago', scenario2)
    .add('already exceeded estimate', scenario3);

function scenario1() {
    const started = moment().toISOString();
    return (
        <LiveStatusIndicator result={'running'} startTime={started} estimatedDuration={10000} />
    );
}

function scenario2() {
    const started = moment().subtract(30, 'seconds').toISOString();
    return (
        <LiveStatusIndicator result={'running'} startTime={started} estimatedDuration={1000*60} />
    );
}

function scenario3() {
    const started = moment().subtract(1, 'minute').toISOString();
    return (
        <LiveStatusIndicator result={'running'} startTime={started} estimatedDuration={1000*30} />
    );
}
