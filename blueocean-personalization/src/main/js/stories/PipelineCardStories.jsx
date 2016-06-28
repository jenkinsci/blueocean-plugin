/**
 * Created by cmeyers on 6/28/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';

import { PipelineCard } from '../components/PipelineCard';

storiesOf('PipelineCard', module)
    .add('test', scenario1);

function scenario1() {
    return (
        <PipelineCard />
    );
}
