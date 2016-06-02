import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { ReadableDate } from '../components/ReadableDate';

storiesOf('ReadableDate', module)
    .add('standard, distant', scenario1)
    .add('standard, recent', scenario2)
    .add('bad data', scenario3);

function scenario1() {
    return (
        <ReadableDate date="2015-05-24T08:57:06.406+0000" />

    );
}

function scenario2() {
    const year = new Date().getFullYear();
    const date = `${year}-05-24T08:57:06.406-0400`;
    return (
        <ReadableDate date={date} />
    );
}

function scenario3() {
    return (
        <ReadableDate date="bad date" />
    );
}