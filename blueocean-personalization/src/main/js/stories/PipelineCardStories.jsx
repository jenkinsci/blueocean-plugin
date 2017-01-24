/**
 * Created by cmeyers on 6/28/16.
 */
import React from 'react';
import { action, storiesOf } from '@kadira/storybook';
import moment from 'moment';
import { DEBUG } from '@jenkins-cd/blueocean-core-js';
DEBUG.enableMocksForI18n();

import { PipelineCard } from '../components/PipelineCard';

const style = { padding: '10px' };
const style2 = { paddingBottom: '10px' };

storiesOf('PipelineCard', module)
    .add('all states', () => {
        const states = 'SUCCESS,QUEUED,RUNNING,FAILURE,ABORTED,UNSTABLE,NOT_BUILT,UNKNOWN'.split(',');
        const startTime = moment().subtract(60, 'seconds').toISOString();
        const estimatedDuration = 1000 * 60 * 5; // 5 mins

        return (
            <div style={style}>
            { states.map(s =>
                <div key={s} style={style2}>
                    <PipelineCard status={s} organization="Jenkins" pipeline="blueocean"
                      branch="feature/JENKINS-123" commitId="447d8e1" favorite
                      onRunClick={action('run')} onFavoriteToggle={action('toggle')}
                    />
                </div>
            ) }
                <PipelineCard status="RUNNING" startTime={startTime} estimatedDuration={estimatedDuration}
                  organization="jenkinsci" pipeline="blueocean" commitId="447d8e1"
                  onRunClick={action('run')} onFavoriteToggle={action('toggle')}
                />
            </div>
        );
    });
