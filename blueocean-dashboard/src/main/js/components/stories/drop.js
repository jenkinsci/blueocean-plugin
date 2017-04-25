import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Dropdown } from '@jenkins-cd/design-language';

const a2z = 'ABCDEFGHIJKLM NOPQRSTUVWXYZ';

const style = {
    padding: 10,
    width: 200,
};

function createOptions(text = 'Option', asObject = false) {
    const options = [];

    for (let index = 0; index < 200; index++) {
        const label = `${text} ${options.length + 1}`;
        options.push(!asObject ? label : { label });
    }
    return options;
}

storiesOf('DropDown', module)
    .add('general', () => (<div>
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

            <div style={{ ...style, maxWidth: 150 }}>
                <p>Truncation</p>

                <Dropdown
                    placeholder="Truncated because the text is too long"
                    options={createOptions(a2z)}
                />
            </div>
        </div>
    ))
;
