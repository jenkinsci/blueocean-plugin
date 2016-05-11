import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { PipelineResult } from '../components';
import { runs } from './data/runs';

storiesOf('PipelineResult', module)
    .add('default', scenario1);

function scenario1() {
    return (
        <div className="dialog">
            <div className="header success">
                <PipelineResult data={runs[0]} />
            </div>
        </div>
    );
}