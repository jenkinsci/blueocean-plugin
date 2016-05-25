import React, {Component, PropTypes} from 'react';
import { storiesOf } from '@kadira/storybook';

import { StatusIndicator } from '../components';

const resultValues = [
    ['Running 0%', 'running', 0],
    ['Running 33%', 'running', 33],
    ['Running 50%', 'running', 50],
    ['Running 99%', 'running', 99],
    ['Running 100%', 'running', 100],
    ['Running 120%', 'running', 120],
    ['Success', 'success'],
    ['Failure', 'failure'],
    ['Queued', 'queued'],
    ['Unstable', 'unstable'],
    ['Aborted', 'aborted'],
    ['Not Built', 'not_built', 77],
    ['Unknown', 'unknown'],
    ['(invalid)', 'invalid_state'],
    ['(with space)', 'blah blah'],
    ['(null)', null],
    ['(gibberish)', 'hjgf%^\'"']
];

storiesOf('StatusIndicator', module)
    .add('all', () => {

        return (
            <table>
                <thead>
                    <tr>
                        <th>Description</th>
                        <th>Value</th>
                        <th>StatusIndicator</th>
                    </tr>
                </thead>
                <tbody>
                    {resultValues.map((testValue, i) => {
                        const [label, value, percentage = 0] = testValue;
                        return (
                            <tr key={i}>
                                <td>{label}</td>
                                <td>{value}</td>
                                <td><StatusIndicator result={value} percentage={percentage}/></td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        );
    });
