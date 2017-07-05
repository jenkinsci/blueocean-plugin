import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { RadioButtonGroup } from '../components/forms/RadioButtonGroup';

storiesOf('RadioButtonGroup', module)
    .add('general', () => <General />)
    .add('labeling', () => <Labeling />)
    .add('callbacks', () => <Callbacks />)
    .add('sizes', () => <Sizes />)
;

const style = {
    padding: 10,
};

const options = ['Alpha', 'Beta', 'Charlie'];

function General() {
    return (
        <div>
            <div style={style}>
                <p>Vertical</p>

                <RadioButtonGroup
                    options={options}
                />
            </div>
            <div style={style}>
                <p>Horizontal</p>

                <RadioButtonGroup
                    className="u-layout-horizontal"
                    options={options}
                />
            </div>
            <div style={style}>
                <p>Disabled</p>

                <RadioButtonGroup
                    options={options}
                    className="u-layout-horizontal"
                    defaultOption="Alpha"
                    disabled
                />
            </div>
            <div style={style}>
                <p>Default Value</p>

                <RadioButtonGroup
                    options={options}
                    className="u-layout-horizontal"
                    defaultOption="Charlie"
                />
            </div>
        </div>
    );
}

function Labeling() {
    const options = [
        { label: 'Foo' },
        { label: 'Bar' },
        { label: 'Baz' },
    ];

    return (
        <div>
            <div style={style}>
                <p>Using labelField='label'</p>

                <RadioButtonGroup
                    options={options}
                    labelField="label"
                />
            </div>
            <div style={style}>
                <p>Using labelFunction</p>

                <RadioButtonGroup
                    options={options}
                    labelFunction={item => `!${item.label}!`}
                />
            </div>
        </div>
    );
}

function Callbacks() {
    return (
        <div style={style}>
            <RadioButtonGroup
                options={['A','B','C']}
                onChange={val => console.log('onChange', val)}
            />
        </div>
    );
}

function Sizes() {
    return (
        <div>
            <div style={style}>
                <p>Using no layout</p>
                <RadioButtonGroup options={options} />
            </div>
            <div className="layout-small" style={style}>
                <p>Using layout-small</p>
                <RadioButtonGroup options={options} />
            </div>
            <div className="layout-medium" style={style}>
                <p>Using layout-medium</p>
                <RadioButtonGroup options={options} />
            </div>
            <div className="layout-large" style={style}>
                <p>Using layout-large</p>
                <RadioButtonGroup options={options} />
            </div>
        </div>
    );
}
