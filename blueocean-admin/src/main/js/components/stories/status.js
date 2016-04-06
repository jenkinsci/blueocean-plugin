import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { StatusIndicator } from '../status/StatusIndicator.jsx';
import RunningIndicator from '../RunningIndicator.jsx';

const props = {
    width: '640px',
    height: '640px',
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
              result="SUCCESS"
            />
            <StatusIndicator
              result="FAILURE"
            />
            <StatusIndicator
              result="QUEUED"
            />
            <StatusIndicator
              result="RUNNING"
              percentage={50}
            />
        </div>
    ))
;
