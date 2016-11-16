import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { RadioButtonGroup } from '../components/forms/RadioButtonGroup';

storiesOf('RadioButtonGroup', module)
    .add('vertical', () => <Vertical />)
    .add('horizontal', () => <Horizontal />)
    .add('default value', () => <DefaultOption />)
    .add('label field', () => <LabelField />)
    .add('label function', () => <LabelFunction />)
    .add('callbacks', () => <Callbacks />)
    .add('sizes', () => <Sizes />)
;

const style = {
    padding: 10,
};

const options = ['Alpha', 'Beta', 'Charlie'];

function Vertical() {
    return (
        <div style={style}>
            <RadioButtonGroup
                options={options}
            />
        </div>
    );
}

function Horizontal() {
    return (
        <div style={style}>
            <RadioButtonGroup
                className="is-layout-horizontal"
                options={options}
            />
        </div>
    );
}

function DefaultOption() {
    return (
        <div style={style}>
            <RadioButtonGroup
                options={options}
                defaultOption="C"
            />
        </div>
    );
}

function LabelField() {
    const options = [
        { label: 'Foo' },
        { label: 'Bar' },
        { label: 'Baz' },
    ];

    return (
        <div style={style}>
            <RadioButtonGroup
                options={options}
                labelField="label"
            />
        </div>
    );
}

function LabelFunction() {
    const options = [
        { label: 'Foo' },
        { label: 'Bar' },
        { label: 'Baz' },
    ];

    return (
        <div style={style}>
            <RadioButtonGroup
                options={options}
                labelFunction={item => `!${item.label}!`}
            />
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
