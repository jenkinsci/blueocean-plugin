/**
 * Created by cmeyers on 6/28/16.
 */
import React from 'react';
import { action, storiesOf } from '@kadira/storybook';

import { PipelineCard } from '../components/PipelineCard';

storiesOf('PipelineCard', module)
    .add('all states', () => {
        const style = { padding: '10px' };
        const style2 = { paddingBottom: '10px' };
        const states = 'SUCCESS,QUEUED,RUNNING,FAILURE,ABORTED,UNSTABLE,NOT_BUILT,UNKNOWN'.split(',');


        return (
            <div style={style}>
            { states.map(s =>
                <div key={s} style={style2}>
                    <PipelineCard status={s} organization="jenkinsci" pipeline="blueocean"
                      branch="master" commitId="447d8e1" favorite
                      onRunClick={action('run')} onFavoriteToggle={action('toggle')}
                    />
                </div>
            ) }
            </div>
        );
    });

