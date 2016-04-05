import React from 'react';
import { storiesOf } from '@kadira/storybook';
import StatusIndicator from '../StatusIndicator.jsx';
import SvgStatus from '../svgStatus.jsx';

/*
 First example of using storybook
 */
storiesOf('StatusIndicators', module)
    .add('success', () => (
        <SvgStatus result="SUCCESS" />
    ))
    .add('failure', () => (
        <SvgStatus result="FAILURE" />
    ))
    .add('queued', () => (
        <SvgStatus result="QUEUED" />
    ))
    .add('running', () => (
        <StatusIndicator result="RUNNING" />
    ))
    .add('no staus null', () => (
        <SvgStatus />
    ))
;
