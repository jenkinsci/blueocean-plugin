import React from 'react';
import { storiesOf } from '@kadira/storybook';
import moment from 'moment';

import { RunningStatusIndicator } from '../components';

storiesOf('RunningStatusIndicator', module)
    .add('10s, start now', scenario1)
    .add('60s, started 30s ago', scenario2);


function scenario1() {
    return (
        <RunningStatusIndicator result={'running'} estimatedDuration={10000} />
    );
}

function scenario2() {
    const started = moment().subtract(30, 'seconds').valueOf();
    return (
        <RunningStatusIndicator result={'running'} startTime={started} estimatedDuration={1000*60} />
    );
}
