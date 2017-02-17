import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Dropdown, Dialog } from '../components';
import Utils from './Utils';

storiesOf('Dropdown', module)
    .add('general', () => <General />)
    .add('In Dialog', () => <InDialog />)
    .add('labeling', () => <LabelOptions />)
    .add('keyboard & focus', () => <KeyboardFocus />)
    .add('callbacks', () => <Callbacks />)
;

const style = {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    padding: 10,
};

const a2z = 'ABCDEFGHIJKLM NOPQRSTUVWXYZ';

function createOptions(text = 'Option', asObject = false) {
    const options = [];

    for (let index = 0; index < 200; index++) {
        const label = `${text} ${options.length + 1}`;
        options.push(!asObject ? label : { label });
    }
    return options;
}

function InDialog() {
    return (
        <div>
            <p>Background page.</p>

            <Dialog title="Not testing the Dialog">
                <div style={{maxWidth: '40em'}}>
                    <p>
                        Class condimentum augue sapien sed a fermentum purus mi a fusce ridiculus
                        ultricies vel vivamus vestibulum nullam consequat et suspendisse montes
                        consectetur enim nam phasellus id faucibus elementum malesuada. Elit
                        aenean dolor adipiscing duis.
                    </p>
                    <General/>
                </div>
            </Dialog>
        </div>
    );
}


function General() {
    Utils.createCssRule(
        '.Dropdown-Default .Dropdown-placeholder',
        'font-style: italic', 'text-transform: uppercase'
    );

    const style = {
        padding: 10,
    };

    return (
        <div>
            <div style={style}>
                <p>Default</p>

                <Dropdown
                    options={createOptions()}
                />
            </div>

            <div style={style}>
                <p>Disabled</p>

                <Dropdown
                    options={createOptions()}
                    disabled
                />
            </div>

            <div style={style}>
                <p>Default Value</p>

                <Dropdown
                    options={createOptions()}
                    defaultOption="Option 3"
                />
            </div>

            <div className="Dropdown-Default" style={style}>
                <p>Placeholder Styling</p>

                <Dropdown
                    options={createOptions()}
                />
            </div>

            <div style={{...style, maxWidth: 150}}>
                <p>Truncation</p>

                <Dropdown
                    placeholder="Truncated because the text is too long"
                    options={createOptions(a2z)}
                />
            </div>
        </div>
    );
}

function LabelOptions() {
    const style = {
        display: 'flex',
        justifyContent: 'space-around',
    };

    return (
        <div style={style}>
            <div>
                <p>Using labelField=label</p>

                <Dropdown
                    labelField="label"
                    options={createOptions('Option', true)}
                />
            </div>
            <div>
                <p>Using labeFunction</p>

                <Dropdown
                    labelFunction={val => `\\m/ ${val.label} \\m/`}
                    options={createOptions('Option', true)}
                />
            </div>
        </div>
    );
}

function KeyboardFocus() {
    const options = createOptions();
    options.unshift(a2z);

    return (
        <div style={{...style, height: 400}}>
            <p>This Layout is useful for demonstrating keyboard accessibility and focus behavior,
            especially as compared to a standard select box.</p>

            <button>Test 1</button>

            <select>
                <option disabled selected>- Select -</option>
                { options.map((opt, index) =>
                    <option key={index}>{opt}</option>
                )}
            </select>

            <button>Test 2</button>

            <Dropdown
                options={options}
            />

            <button>Test 3</button>
        </div>
    );
}

function Callbacks() {
    return (
        <div style={{...style, height: 100}}>
            <Dropdown
                options={createOptions()}
                onChange={(val, index) => console.log(`onChange val=${val}, index=${index}`)}
            />
        </div>
    );
}
