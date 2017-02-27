import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { TimeDuration } from '../components/TimeDuration';

storiesOf('TimeDuration', module)
    .add('<1s', lessThan1s)
    .add('short duration', standard)
    .add('short duration - locale de', standardDe)
    .add('live update', liveUpdate)
    .add('live update - locale de', liveUpdateDe)
    .add('long duration, all date parts', complexHint)
    .add('long duration, all date parts - locale de', complexHintDe)
    .add('with custom hint', customHint);

function lessThan1s() {
    return (
        <TimeDuration millis={999} />
    );
}

function standard() {
    return (
        <TimeDuration millis={50000} />
    );
}

function standardDe() {
    return (
        <TimeDuration
            millis={50000}
            locale="de"
            hintFormat="M [mos], d [Tage], h[Std.], m[m], s[s]"
            liveFormat="m[ Minuten] s[ Sekunden]"
        />
    );
}

function liveUpdate() {
    return (
        <TimeDuration millis={50000} liveUpdate />
    );
}

function liveUpdateDe() {
    return (
        <TimeDuration
            liveUpdate
            updatePeriod={3000}
            millis={50000}
            locale="de"
            hintFormat="M [mos], d [Tage], h[Std.], m[m], s[s]"
            liveFormat="m[ Minuten] s[ Sekunden]"
        />
    );
}

function complexHint() {
    return (
        <TimeDuration millis={1000*60*60*24*7*4*6+1001*60*60*4.75} />
    );
}

function complexHintDe() {
    return (
        <TimeDuration
            updatePeriod={3000}
            millis={3.5*1000*60*60*24*7*4*6+1001*60*60*4.75}
            locale="de"
            hintFormat="M [Monate], d [Tage], h[h], m[m], s[s]"
            liveFormat="m[ Minuten] s[ Sekunden]"
        />
    );
}

function customHint() {
    return (
        <TimeDuration millis={50000} hint="Custom hint." />
    );
}
