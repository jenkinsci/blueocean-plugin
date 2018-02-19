import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { LiveStatusIndicator } from '../components';

storiesOf('Indicator with heading', module)
    .add('paused', scenario1)
    .add('success', scenario2);

function scenario1() {
    return (
        <div className="dialog">
            <div className="header paused">
                <LiveStatusIndicator result="PAUSED" />
            </div>
        </div>
    );
}
function scenario2() {
    return (
        <div className="dialog">
            <div className="header success">
                <LiveStatusIndicator result="SUCCESS" />
            </div>
        </div>
    );
}
