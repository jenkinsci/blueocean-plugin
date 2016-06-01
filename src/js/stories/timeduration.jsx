import React from 'react';
import { action, storiesOf } from '@kadira/storybook';
import { TimeDuration } from '../components/TimeDuration';

storiesOf('TimeDuration', module)
    .add('short duration', scenario1)
    .add('long duration, all date parts', scenario2)
    .add('with custom hint', scenario3)
    .add('duration as string', scenario4)
    .add('bad value', scenario5)
    .add('undefined value', scenario6);

function scenario1() {
    return (
        <TimeDuration millis={50000} />
    );
}

function scenario2() {
    return (
        <TimeDuration millis={1000*60*60*24*7*4*6+1001*60*60*4.75} />
    );
}

function scenario3() {
    return (
        <TimeDuration millis={50000} hint="Custom hint." />
    );
}

function scenario4() {
    return (
        <TimeDuration millis="50000" />
    );
}

function scenario5() {
    return (
        <TimeDuration millis="abcdefg" />
    );
}

function scenario6() {
    return (
        <TimeDuration />
    );
}