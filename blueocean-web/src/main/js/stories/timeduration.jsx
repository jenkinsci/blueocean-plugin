import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { TimeDuration } from '../components/TimeDuration';

const stringsEn = {
    "common.date.duration.display.format": "M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]",
    "common.date.duration.format": "m[ minutes] s[ seconds]",
    "common.date.duration.hint.format": "M [month], d [days], h[h], m[m], s[s]",
};

const stringsDe = {
    "common.date.duration.display.format": "M [mos], d [Tage], h[Std.], m[m], s[s]",
    "common.date.duration.format": "m[ Minuten] s[ Sekunden]",
    "common.date.duration.hint.format": "M [Monate], d [Tage], h[Std.], m[m], s[s]",
};

const translationsEn = (key) => stringsEn[key] || key;
const translationsDe = (key) => stringsDe[key] || key;


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
        <TimeDuration millis={999} t={translationsEn} />
    );
}

function standard() {
    return (
        <TimeDuration millis={50000} t={translationsEn} />
    );
}

function standardDe() {
    return (
        <TimeDuration
            millis={50000}
            locale="de"
            t={translationsDe}
        />
    );
}

function liveUpdate() {
    return (
        <TimeDuration millis={50000} liveUpdate t={translationsEn} />
    );
}

function liveUpdateDe() {
    return (
        <TimeDuration
            liveUpdate
            updatePeriod={1000}
            millis={50000}
            locale="de"
            t={translationsDe}
        />
    );
}

function complexHint() {
    return (
        <TimeDuration millis={1000*60*60*24*7*4*6+1001*60*60*4.75} t={translationsEn} />
    );
}

function complexHintDe() {
    return (
        <TimeDuration
            updatePeriod={3000}
            millis={3.5*1000*60*60*24*7*4*6+1001*60*60*4.75}
            locale="de"
            t={translationsDe}
        />
    );
}

function customHint() {
    return (
        <TimeDuration millis={50000} hint="Custom hint." t={translationsEn} />
    );
}
