import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { StatusIndicator } from '@jenkins-cd/design-language';
import RunningIndicator from '../RunningIndicator.jsx';

const props = {
    width: '640px',
    height: '640px',
};

const smaller = {
    width: '320px',
    height: '320px',
};

storiesOf('StatusIndicators', module)
    .add('success', () => (
        <StatusIndicator
          {...Object.assign({
              result: 'SUCCESS',
          }, props)}
        />
    ))
    .add('failure', () => (
        <StatusIndicator
          {...Object.assign({
              result: 'FAILURE',
          }, props)}
        />
    ))
    .add('queued', () => (
        <div>
            <div>This will be animated
                by css and will turn
            </div>
            <StatusIndicator
              {...Object.assign({
                  result: 'QUEUED',
              }, props)}
            />
        </div>
    ))
    .add('running', () => (
        <div>
            <div>This shows 50%</div>
            <StatusIndicator
              {...Object.assign({
                  result: 'RUNNING',
                  percentage: 50,
              }, props)}
            />
        </div>
    ))
    .add('running animated', () => (
        <div>
            <div>
                This shows demo where % is raised
                and stops at 100%
            </div>
            <RunningIndicator {...props} />
        </div>
    ))
    .add('all', () => (
        <div>
            <StatusIndicator
              {...smaller}
              result="SUCCESS"
            />
            <StatusIndicator
              {...smaller}
              result="FAILURE"
            />
            <StatusIndicator
              {...smaller}
              result="QUEUED"
            />
            <StatusIndicator
              {...smaller}
              result="RUNNING"
              percentage={50}
            />
        </div>
    ))
;
