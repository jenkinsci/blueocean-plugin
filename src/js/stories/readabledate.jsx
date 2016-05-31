import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { ReadableDate } from '../components/ReadableDate';

storiesOf('ReadableDate', module)
    .add('standard, now', scenario1)
    .add('standard, arbitrary', scenario2)
    .add('bad data', scenario3)
    .add('undefined data', scenario4);

function scenario1() {
    return (
        <ReadableDate date={new Date().toISOString()} />
    );
}

function scenario2() {
    return (
        <ReadableDate date="2016-05-24T08:57:06.406-0400" />
    );
}

function scenario3() {
    return (
        <ReadableDate date="bad date" />
    );
}

function scenario4() {
    return (
        <ReadableDate />
    );
}