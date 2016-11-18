import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Dropdown } from '../components';

storiesOf('Dropdown', module)
    .add('default', () => <Default />)
    .add('keyboard & focus', () => <KeyboardFocus />)
    .add('callbacks', () => <Callbacks />)
;

const style = {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    height: 400,
    padding: 10,
};

function createOptions(count) {
    const options = [];
    options.push('ABCDEFGHIJKLMNOPQRSTUVWXYZ');
    for (let index = 0; index < count; index++) {
        options.push(`Option ${options.length + 1}`);
    }
    return options;
}

function Default() {
    return (
        <div style={{...style, height: 100}}>
            <Dropdown
                options={createOptions(200)}
            />
        </div>
    );
}

function KeyboardFocus() {
    return (
        <div style={style}>
            <p>This Layout is useful for demonstrating keyboard accessibility and focus behavior,
            especially as compared to a standard select box.</p>

            <button>Test 1</button>

            <select>
                <option disabled selected>- Select -</option>
                { createOptions(200).map((option, index) =>
                    <option key={index}>{option}</option>
                )}
            </select>

            <button>Test 2</button>

            <Dropdown
                options={createOptions(200)}
            />

            <button>Test 3</button>
        </div>
    );
}

function Callbacks() {
    return (
        <div style={{...style, height: 100}}>
            <Dropdown
                options={createOptions(200)}
                onChange={(val, index) => console.log(`onChange val=${val}, index=${index}`)}
            />
        </div>
    );
}
