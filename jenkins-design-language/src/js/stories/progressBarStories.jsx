import React from 'react';
import { storiesOf } from '@kadira/storybook';

import { Progress } from '../components';

//
const resultValues = [
    ['Running 0%', 0],
    ['Running 33%', 33],
    ['Running 50%', 50],
    ['Running 99%', 99],
    ['Running 100%', 100],
    ['Running 120%', 120],
    ['Indeterminate'],
];



storiesOf('Progress', module)
    .add('indeterminate', progressBarStory)
    .add('all', progressBarStories);

function progressBarStory() {
    return ( <div>
        Indeterminate state <Progress/>
    </div>);
}

function progressBarStories() {
    return (
        <table style={{width: "98%"}}>
            <thead>
                <tr>
                    <th>Description</th>
                    <th>StatusIndicator</th>
                </tr>
            </thead>
            <tbody>
                {resultValues.map((testValue, i) => {
                    const [label, percentage] = testValue;
                    return (
                        <tr key={i}>
                            <td>{label}</td>
                            <td><Progress percentage={percentage}/></td>
                        </tr>
                    );
                })}
            </tbody>
        </table>
    );
}
