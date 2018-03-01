/* eslint-disable */

import { storiesOf } from '@kadira/storybook';
import React from 'react';
import {ResultItem} from '@jenkins-cd/design-language';
import { DownstreamRunsView } from '../downstream-runs/DownstreamRunsView';

storiesOf('Downstream Build Links', module)
    .add('Basic', basic)
;

const strings = {
    "common.date.duration.display.format": "M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]",
    "common.date.duration.hint.format": "M [month], d [days], h[h], m[m], s[s]",
};

const t = (key) => {
    if (!(key in strings)) {
        console.log('missing key', key);
        strings[key] = key;
    }
    return strings[key];
};

function basic() {

    const runs = [
        {
            runLink: '/blue/rest/organizations/jenkins/pipelines/downstream1/runs/105/',
            runDescription: 'downstream1 #105',
        },
        {
            runLink: '/blue/rest/organizations/jenkins/pipelines/downstream1/runs/106/',
            runDescription: 'downstream1 #106',
        },
        {
            runLink: '/blue/rest/organizations/jenkins/pipelines/downstream1/runs/65/',
            runDescription: 'downstream2 #65',
            runDetails: {
                durationInMillis: 26479,
                enQueueTime: '2018-01-11T16:36:18.556+1000',
                endTime: '2018-01-11T16:36:45.037+1000',
                estimatedDurationInMillis: 28904,
                id: '13',
                organization: 'jenkins',
                pipeline: 'Downstream1',
                result: 'SUCCESS',
                runSummary: 'stable',
                startTime: '2018-01-11T16:36:18.558+1000',
                state: 'FINISHED',
            },
        },
        {
            runLink: '/blue/rest/organizations/jenkins/pipelines/downstream1/runs/11/',
            runDescription: 'downstream2 #11',
            runDetails: {
                durationInMillis: 26479,
                enQueueTime: '2018-01-11T16:36:18.556+1000',
                endTime: null, //'2018-01-11T16:36:45.037+1000',
                estimatedDurationInMillis: 28904,
                id: '17',
                organization: 'jenkins',
                pipeline: 'Downstream2',
                result: 'UNKNOWN',
                runSummary: 'stable',
                startTime: '2018-01-11T16:36:18.558+1000',
                state: 'RUNNING',
            },
        },
        {
            runLink: '/blue/rest/organizations/jenkins/pipelines/downstream1/runs/66/',
            runDescription: 'downstream2 #66',
        },
    ];

    return (
        <div style={{ padding: '1em' }}>
            <strong>Some Excellent Stage</strong>
            <div style={{height:'0.5em'}}/>
            <ResultItem result="success" label="Alpha" extraInfo="Bravo"/>
            <ResultItem result="success" label="Charlie" extraInfo="Delta"/>
            <ResultItem result="success" label="Echo" extraInfo="Foxtrot"/>
            <ResultItem result="success" label="Golf" extraInfo="Hotel"/>
            <div style={{height:'2em'}}/>
            <strong>Triggered Builds</strong>
            <div style={{height:'0.5em'}}/>
            <DownstreamRunsView runs={runs} t={t} />
        </div>
    );
}
