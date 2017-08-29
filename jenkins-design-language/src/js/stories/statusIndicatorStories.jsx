import React from 'react';
import { storiesOf } from '@kadira/storybook';

import { StatusIndicator } from '../components';
import { getGlyphFor } from '../components/status/SvgStatus';


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
    ['Skipped', 'skipped'],
    ['Unknown', 'unknown'],
    ['Paused', 'paused'],
    ['(invalid)', 'invalid_state'],
    ['(with space)', 'blah blah'],
    ['(null)', null],
    ['(gibberish)', 'hjgf%^\'"']
];

const glyphResultValues = resultValues.slice(5); // Just skip all the repeats of "running"

storiesOf('StatusIndicator', module)
    .add('all', statusIndicatorStories)
    .add('status glyphs', glyphStories)
    .add('no bg, large', noBgLargeStories);

function statusIndicatorStories() {
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
}

function glyphStories() {
    return (
        <table>
            <thead>
                <tr>
                    <th>Description</th>
                    <th>Value</th>
                    <th>Glyph</th>
                </tr>
            </thead>
            <tbody>
                {glyphResultValues.map((testValue, i) => {
                    const [label, value] = testValue;
                    const glyph = getGlyphFor(value);
                    return (
                        <tr key={i}>
                            <td>{label}</td>
                            <td>{value}</td>
                            <td><svg width="24" height="24">
                                {/* Grid fakeout */}
                                <rect x="0" y="0" height="6" width="6" fill="#eee"/>
                                <rect x="12" y="0" height="6" width="6" fill="#eee"/>
                                <rect x="6" y="6" height="6" width="6" fill="#eee"/>
                                <rect x="18" y="6" height="6" width="6" fill="#eee"/>
                                <rect x="0" y="12" height="6" width="6" fill="#eee"/>
                                <rect x="12" y="12" height="6" width="6" fill="#eee"/>
                                <rect x="6" y="18" height="6" width="6" fill="#eee"/>
                                <rect x="18" y="18" height="6" width="6" fill="#eee"/>
                                {/* Show glyph */}
                                <g transform="translate(12 12)">{glyph}</g>
                            </svg></td>
                        </tr>
                    );
                })}
            </tbody>
        </table>
    );
}

function noBgLargeStories() {
    const styleCell = {
        display: 'flex',
    };
    const styleIndicator = {
        backgroundColor: '#eee',
        border: '1px solid black',
    };
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
                        <td style={styleCell}>
                            <span style={styleIndicator}>
                                <StatusIndicator noBackground width="100px" height="100px"
                                  result={value} percentage={percentage}
                                />
                            </span>
                        </td>
                    </tr>
                );
            })}
            </tbody>
        </table>
    );
}
