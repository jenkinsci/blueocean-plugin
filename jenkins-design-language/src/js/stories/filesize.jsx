import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { FileSize } from '../components/FileSize';

storiesOf('FileSize', module)
    .add('0 bytes', scenario0)
    .add('5 bytes', scenario1)
    .add('1 kilobyte', scenario2)
    .add('5.5 kilobyte', scenario3)
    .add('102.5 megs', scenario4)
    .add('5500 string', scenario5)
    .add('bogus', scenario6)
    .add('large', scenario7)
    .add('negative', scenario8);

function scenario0() {
    return (
        <FileSize bytes={0} />
    );
}

function scenario1() {
    return (
        <FileSize bytes={5} />
    );
}

function scenario2() {
    return (
        <FileSize bytes={1024} />
    );
}

function scenario3() {
    return (
        <FileSize bytes={1024*5.5} />
    );
}

function scenario4() {
    return (
        <FileSize bytes={1024*1024*102.5} />
    );
}

function scenario5() {
    return (
        <FileSize bytes="5500" />
    );
}

function scenario6() {
    return (
        <FileSize bytes="bogus" />
    );
}

function scenario7() {
    return (
        <FileSize bytes={1024 * Math.pow(10,20)} />
    );
}

function scenario8() {
    return (
        <FileSize bytes={-1024 * 1024 * 5.5} />
    );
}
