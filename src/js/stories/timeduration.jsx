import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { TimeDuration } from '../components/TimeDuration';

storiesOf('TimeDuration', module)
    .add('short duration', standard)
    .add('live update', liveUpdate)
    .add('long duration, all date parts', complexHint)
    .add('with custom hint', customHint);

function standard() {
    return (
        <TimeDuration millis={50000} />
    );
}

function liveUpdate() {
    return (
        <TimeDuration millis={50000} liveUpdate />
    );
}

function complexHint() {
    return (
        <TimeDuration millis={1000*60*60*24*7*4*6+1001*60*60*4.75} />
    );
}

function customHint() {
    return (
        <TimeDuration millis={50000} hint="Custom hint." />
    );
}
