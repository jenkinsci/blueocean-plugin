import React from 'react';
import { storiesOf } from '@kadira/storybook';
import moment from 'moment';

import { LiveStatusIndicator } from '../components';

storiesOf('LiveStatusIndicator', module)
    .add('10s, start now', scenario1)
    .add('60s, started 30s ago', scenario2)
    .add('already exceeded estimate', scenario3)
    .add('no estimate available', scenario4)
    .add('no bg, large', scenario5);

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

function scenario4() {
    const started = moment().subtract(1, 'minute').toISOString();
    return (
        <LiveStatusIndicator result={'running'} startTime={started} estimatedDuration={-1} />
    );
}

function scenario5() {
    const started = moment().toISOString();
    const styleContainer = {
        display: 'flex',
        justifyContent: 'space-around',
        margin: '20px',
    };
    const styleIndicator = {
        backgroundColor: '#0071C4',
        border: '1px solid black',
    };
    return (
        <div style={styleContainer}>
            <span style={styleIndicator} className="inverse">
                <LiveStatusIndicator result={'running'} noBackground
                  width="100px" height="100px"
                />
            </span>
            <span style={styleIndicator}>
                <LiveStatusIndicator result={'success'} noBackground
                  startTime={started} estimatedDuration={10000}
                  width="100px" height="100px"
                />
            </span>
        </div>
    );
}
